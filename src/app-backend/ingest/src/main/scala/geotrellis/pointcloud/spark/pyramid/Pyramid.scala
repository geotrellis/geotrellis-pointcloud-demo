package geotrellis.pointcloud.spark.pyramid

import geotrellis.spark._
import geotrellis.spark.tiling._
import geotrellis.raster._
import geotrellis.util._

import org.apache.spark.Partitioner
import org.apache.spark.rdd._
import com.typesafe.scalalogging.LazyLogging
import com.vividsolutions.jts.geom.Coordinate
import geotrellis.vector.triangulation.DelaunayTriangulation
import spire.syntax.cfor.cfor

import scala.reflect.ClassTag

object Pyramid extends LazyLogging {
  case class Options(decimation: Double = 0.75, partitioner: Option[Partitioner] = None)
  object Options {
    def DEFAULT = Options()
    implicit def partitionerToOptions(p: Partitioner): Options = Options(partitioner = Some(p))
    implicit def optPartitionerToOptions(p: Option[Partitioner]): Options = Options(partitioner = p)
    implicit def decimationToOptions(d: Double): Options = Options(decimation = d)
  }

  def up[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K, Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    zoom: Int,
    options: Options
   ): (Int, RDD[(K,  Array[Coordinate])] with Metadata[M]) = {
    val Options(decimation, partitioner) = options

    val sourceLayout = rdd.metadata.getComponent[LayoutDefinition]
    val sourceBounds = rdd.metadata.getComponent[Bounds[K]]
    val LayoutLevel(nextZoom, nextLayout) = layoutScheme.zoomOut(LayoutLevel(zoom, sourceLayout))

    val nextKeyBounds =
      sourceBounds match {
        case EmptyBounds => EmptyBounds
        case kb: KeyBounds[K] =>
          // If we treat previous layout as extent and next layout as tile layout we are able to hijack MapKeyTransform
          // to translate the spatial component of source KeyBounds to next KeyBounds
          val extent = sourceLayout.extent
          val sourceRe = RasterExtent(extent, sourceLayout.layoutCols, sourceLayout.layoutRows)
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
    def createTiles(tile: (K,  Array[Coordinate])): Seq[(K,  Array[Coordinate])]                             = Seq(tile)
    def mergeTiles1(tiles: Seq[(K,  Array[Coordinate])], tile: (K,  Array[Coordinate])): Seq[(K,  Array[Coordinate])]         = tiles :+ tile
    def mergeTiles2(tiles1: Seq[(K,  Array[Coordinate])], tiles2: Seq[(K,  Array[Coordinate])]): Seq[(K,  Array[Coordinate])] = tiles1 ++ tiles2

    val nextRdd = {
      val transformedRdd = rdd
        .map { case (key, tile) =>
          val extent = sourceLayout.mapTransform(key)
          val newSpatialKey = nextLayout.mapTransform(extent.center)
          (key.setComponent(newSpatialKey), (key, tile))
        }

      partitioner
        .fold(transformedRdd.combineByKey(createTiles, mergeTiles1, mergeTiles2))(transformedRdd.combineByKey(createTiles _, mergeTiles1 _, mergeTiles2 _, _))
        .map { case (newKey: K, seq: Seq[(K,  Array[Coordinate])]) =>
          val pts = seq.flatMap(_._2).toArray
          val length = pts.length
          val by = (decimation * length).toInt
          val newLength = length - by
          val arr = new Array[Coordinate](newLength)
          val dt = DelaunayTriangulation(pts)
          dt.decimate(by)

          val ps = dt.pointSet
          cfor(0)(_ < newLength, _ + 1) { i =>
            arr(i) = ps.getCoordinate(i)
          }

          newKey -> arr
        }.filter { _._2.length > 2 }
    }

    nextZoom -> new ContextRDD(nextRdd, nextMetadata)
  }

  def up[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    zoom: Int
   ): (Int, RDD[(K,  Array[Coordinate])] with Metadata[M]) =
    up(rdd, layoutScheme, zoom, Options.DEFAULT)

  def levelStream[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    endZoom: Int,
    options: Options
   ): Stream[(Int, RDD[(K,  Array[Coordinate])] with Metadata[M])] =
    (startZoom, rdd) #:: {
      if (startZoom > endZoom) {
        val (nextZoom, nextRdd) = Pyramid.up(rdd, layoutScheme, startZoom, options)
        levelStream(nextRdd, layoutScheme, nextZoom, endZoom, options)
      } else {
        Stream.empty
      }
    }

  def levelStream[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    endZoom: Int
   ): Stream[(Int, RDD[(K,  Array[Coordinate])] with Metadata[M])] =
    levelStream(rdd, layoutScheme, startZoom, endZoom, Options.DEFAULT)

  def levelStream[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    options: Options
   ): Stream[(Int, RDD[(K,  Array[Coordinate])] with Metadata[M])] =
    levelStream(rdd, layoutScheme, startZoom, 0, options)

  def levelStream[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int
   ): Stream[(Int, RDD[(K,  Array[Coordinate])] with Metadata[M])] =
    levelStream(rdd, layoutScheme, startZoom, Options.DEFAULT)

  def upLevels[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    endZoom: Int,
    options: Options
   )(f: (RDD[(K,  Array[Coordinate])] with Metadata[M], Int) => Unit): RDD[(K,  Array[Coordinate])] with Metadata[M] = {
    def runLevel(thisRdd: RDD[(K,  Array[Coordinate])] with Metadata[M], thisZoom: Int): (RDD[(K,  Array[Coordinate])] with Metadata[M], Int) =
      if (thisZoom > endZoom) {
        f(thisRdd, thisZoom)
        val (nextZoom, nextRdd) = Pyramid.up(thisRdd, layoutScheme, thisZoom, options)
        runLevel(nextRdd, nextZoom)
      } else {
        f(thisRdd, thisZoom)
        (thisRdd, thisZoom)
      }

    runLevel(rdd, startZoom)._1
  }

  def upLevels[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    endZoom: Int
   )(f: (RDD[(K,  Array[Coordinate])] with Metadata[M], Int) => Unit): RDD[(K,  Array[Coordinate])] with Metadata[M] =
    upLevels(rdd, layoutScheme, startZoom, endZoom, Options.DEFAULT)(f)

  def upLevels[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int,
    options: Options
   )(f: (RDD[(K,  Array[Coordinate])] with Metadata[M], Int) => Unit): RDD[(K,  Array[Coordinate])] with Metadata[M] =
    upLevels(rdd, layoutScheme, startZoom, 0, options)(f)

  def upLevels[
    K: SpatialComponent: ClassTag,
    M: Component[?, LayoutDefinition]: Component[?, Bounds[K]]
  ](rdd: RDD[(K,  Array[Coordinate])] with Metadata[M],
    layoutScheme: LayoutScheme,
    startZoom: Int
   )(f: (RDD[(K,  Array[Coordinate])] with Metadata[M], Int) => Unit): RDD[(K,  Array[Coordinate])] with Metadata[M] =
    upLevels(rdd, layoutScheme, startZoom, Options.DEFAULT)(f)
}

