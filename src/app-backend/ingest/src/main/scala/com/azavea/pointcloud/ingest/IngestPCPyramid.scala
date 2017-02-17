package com.azavea.pointcloud.ingest

import com.azavea.pointcloud.ingest.conf.IngestConf
import com.vividsolutions.jts.geom.Coordinate
import io.pdal._
import geotrellis.pointcloud.pipeline._
import geotrellis.pointcloud.spark._
import geotrellis.pointcloud.spark.io._
import geotrellis.pointcloud.spark.io.hadoop._
import geotrellis.pointcloud.spark.io.s3._
import geotrellis.pointcloud.spark.tiling.Implicits.{withTilerMethods => withPCTilerMethods}
import geotrellis.proj4.CRS
import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.index.ZCurveKeyIndexMethod
import geotrellis.spark.io.kryo.KryoRegistrator
import geotrellis.spark.io.s3.S3LayerWriter
import geotrellis.spark.tiling._
import geotrellis.util._
import geotrellis.vector._
import geotrellis.vector.triangulation._
import org.apache.hadoop.fs.Path
import org.apache.spark.rdd.RDD
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.{SparkConf, SparkContext}
import spire.syntax.cfor.cfor

import scala.collection.mutable

object IngestPCPyramid {
  type PointCloudLayerRDD[K] = RDD[(SpatialKey, Array[Coordinate])] with Metadata[TileLayerMetadata[K]]

  def main(args: Array[String]): Unit = {
    val opts      = IngestConf.parse(args)
    // val chunkPath = System.getProperty("user.dir") + "/chunks/"

    val conf = new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("PointCloudCount")
      .set("spark.local.dir", "/data/spark")
      .set("spark.serializer", classOf[KryoSerializer].getName)
      .set("spark.kryo.registrator", classOf[KryoRegistrator].getName)

    implicit val sc = new SparkContext(conf)

    try {
      val pipeline = Read("", opts.inputCrs) ~
        ReprojectionFilter(opts.destCrs) ~
        opts.maxValue.map { v => RangeFilter(Some(s"Z[0:$v]")) }

      val source =
        if(opts.nonS3Input)
          HadoopPointCloudRDD(
            new Path(opts.inputPath),
            HadoopPointCloudRDD.Options.DEFAULT.copy(pipeline = pipeline)
          ).map { case (header, pc) => (header: PointCloudHeader, pc) } //.cache()
        else
          S3PointCloudRDD(
            bucket = opts.S3InputPath._1,
            prefix = opts.S3InputPath._2,
            S3PointCloudRDD.Options.DEFAULT.copy(pipeline = pipeline)
          ).map { case (header, pc) => (header: PointCloudHeader, pc) } //.cache

      val (extent, crs) =
        source
          .map { case (header, _) => (header.projectedExtent3D.extent3d.toExtent, header.crs) }
          .reduce { case ((e1, c), (e2, _)) => (e1.combine(e2), c) }

      val targetCrs = CRS.fromName(opts.destCrs)

      val targetExtent =
        opts.extent match {
          case Some(e) => if (crs.epsgCode != targetCrs.epsgCode) e.reproject(crs, targetCrs) else e
          case _ =>  if (crs.epsgCode != targetCrs.epsgCode) extent.reproject(crs, targetCrs) else extent
        }

      val layoutScheme = if (opts.pyramid || opts.zoomed) ZoomedLayoutScheme(targetCrs) else FloatingLayoutScheme(512)

      val LayoutLevel(zoom, layout) = layoutScheme.levelFor(targetExtent, opts.cellSize)
      val mapTransform = layout.mapTransform
      val kb = KeyBounds(mapTransform(targetExtent))
      val md = TileLayerMetadata[SpatialKey](FloatConstantNoDataCellType, layout, targetExtent, targetCrs, kb)

      val cut: RDD[(SpatialKey, Array[Coordinate])] =
        source
          .flatMap { case (header, pointClouds) =>
            var lastKey: SpatialKey = null
            val keysToPoints = mutable.Map[SpatialKey, mutable.ArrayBuffer[Coordinate]]()

            for (pointCloud <- pointClouds) {
              val len = pointCloud.length
              cfor(0)(_ < len, _ + 1) { i =>
                val x = pointCloud.getX(i)
                val y = pointCloud.getY(i)
                val z = pointCloud.getZ(i)
                val p = new Coordinate(x, y, z)
                val key = mapTransform(x, y)
                if (key == lastKey) {
                  keysToPoints(lastKey) += p
                } else if (keysToPoints.contains(key)) {
                  keysToPoints(key) += p
                  lastKey = key
                } else {
                  keysToPoints(key) = mutable.ArrayBuffer(p)
                  lastKey = key
                }
              }
            }

            keysToPoints.map { case (k, v) => (k, v.toArray) }
          }
          .reduceByKey({ (p1, p2) => p1 ++ p2 }, opts.numPartitions)
          .filter { _._2.length > 2 }

      val layer: PointCloudLayerRDD[SpatialKey] = ContextRDD(cut, md)

      // layer.cache()

      def buildPyramid(zoom: Int, rdd: PointCloudLayerRDD[SpatialKey])
                      (sink: (PointCloudLayerRDD[SpatialKey], Int) => Unit): List[(Int, PointCloudLayerRDD[SpatialKey])] = {
        println(s":::buildPyramid: $zoom")
        if (zoom >= opts.minZoom) {
          // rdd.cache()
          sink(rdd, zoom)
          val LayoutLevel(nextZoom, nextLayout) = layoutScheme.zoomOut(LayoutLevel(zoom, layout))

          val nextKeyBounds =
            kb match {
              //case EmptyBounds => EmptyBounds
              case kb: KeyBounds[SpatialKey] =>
                // If we treat previous layout as extent and next layout as tile layout we are able to hijack MapKeyTransform
                // to translate the spatial component of source KeyBounds to next KeyBounds
                val extent = layout.extent
                val sourceRe = RasterExtent(extent, layout.layoutCols, layout.layoutRows)
                val targetRe = RasterExtent(extent, nextLayout.layoutCols, nextLayout.layoutRows)
                val SpatialKey(sourceColMin, sourceRowMin) = kb.minKey.getComponent[SpatialKey]
                val SpatialKey(sourceColMax, sourceRowMax) = kb.maxKey.getComponent[SpatialKey]
                val (colMin, rowMin) = {
                  val (x, y) = sourceRe.gridToMap(sourceColMin, sourceRowMin)
                  targetRe.mapToGrid(x, y)
                }

                val (colMax, rowMax) = {
                  val (x, y) = sourceRe.gridToMap(sourceColMax, sourceRowMax)
                  targetRe.mapToGrid(x, y)
                }

                KeyBounds(
                  kb.minKey.setComponent(SpatialKey(colMin, rowMin)),
                  kb.maxKey.setComponent(SpatialKey(colMax, rowMax)))
            }

          val nextMetadata =
            rdd.metadata
              .setComponent(nextLayout)
              .setComponent(nextKeyBounds)

          // Functions for combine step
          def createTiles(tile: (SpatialKey, Array[Coordinate])): Seq[(SpatialKey, Array[Coordinate])]                             = Seq(tile)
          def mergeTiles1(tiles: Seq[(SpatialKey, Array[Coordinate])], tile: (SpatialKey, Array[Coordinate])): Seq[(SpatialKey, Array[Coordinate])]         = tiles :+ tile
          def mergeTiles2(tiles1: Seq[(SpatialKey, Array[Coordinate])], tiles2: Seq[(SpatialKey, Array[Coordinate])]): Seq[(SpatialKey, Array[Coordinate])] = tiles1 ++ tiles2

          val nextRdd = {
            val transformedRdd = rdd
              .map { case (key, arr) =>
                val extent = layout.mapTransform(key)
                val newSpatialKey = nextLayout.mapTransform(extent.center)
                (key.setComponent(newSpatialKey), (key, arr))
              }

            transformedRdd.combineByKey(createTiles, mergeTiles1, mergeTiles2)
              .map { case (newKey: SpatialKey, seq: Seq[(SpatialKey, Array[Coordinate])]) =>
                val pts = seq.flatMap(_._2).toArray
                val length = pts.length
                val by = (0.75 * length).toInt
                val newLength = length - by
                val arr = new Array[Coordinate](newLength)
                val dt = DelaunayTriangulation(pts)
                dt.decimate(by)

                val ps = dt.pointSet
                cfor(0)(_ < newLength, _ + 1) { i =>
                  arr(i) = ps.getCoordinate(i)
                }

                newKey -> arr
              }
          }

          val nextCRdd = new ContextRDD(nextRdd, nextMetadata)

          (nextZoom, nextCRdd) :: buildPyramid(nextZoom, nextCRdd)(sink)

          /*val nextRdd = rdd.map { case (key, pts) =>
            val length = pts.length
            val dt = DelaunayTriangulation(pts)
            dt.decimate((0.75 * length).toInt)

            val ps = dt.pointSet
            val arr = new Array[Coordinate](length)
            cfor(0)(_ < length, _ + 1) { i =>
              arr(i) = ps.getCoordinate(i)
            }
            
            key -> arr
          }*/

          // buildPyramid(nextZoom, nextRdd)(sink)
          // val pyramidLevel @ (nextZoom, nextRdd) = Pyramid.up(rdd, layoutScheme, zoom, Pyramid.Options(Bilinear))
          // pyramidLevel :: buildPyramid(nextZoom, nextRdd)(sink)
        } else {
          sink(rdd, zoom)
          List((zoom, rdd))
        }
      }

      if(opts.persist) {
        val writer =
          if(opts.nonS3Catalog) HadoopLayerWriter(new Path(opts.catalogPath))
          else S3LayerWriter(opts.S3CatalogPath._1, opts.S3CatalogPath._2)

        if (opts.pyramid) {

        }

        writer
          .write[SpatialKey, PointCloud, TileLayerMetadata[SpatialKey]](
            LayerId(opts.layerName, 0),
            layer,
            ZCurveKeyIndexMethod
          )
      } else layer.count()

      layer.unpersist(blocking = false)
      source.unpersist(blocking = false)

    } finally sc.stop()
  }
}
