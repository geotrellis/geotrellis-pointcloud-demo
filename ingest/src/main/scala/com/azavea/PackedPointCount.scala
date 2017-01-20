package com.azavea

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}

import geotrellis.raster.TileLayout
import io.pdal._
import geotrellis.pointcloud.spark._
import geotrellis.pointcloud.spark.io._
import geotrellis.pointcloud.spark.io.hadoop._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.io.kryo.KryoRegistrator
import geotrellis.spark.tiling.LayoutDefinition
import geotrellis.vector.Extent
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark._
import org.apache.hadoop.fs.Path
import spire.syntax.cfor._

object PackedPointCount {
  def timed[T](msg: String)(f: => T): T = {
    val s = System.currentTimeMillis
    val result = f
    val e = System.currentTimeMillis
    val t = "%,d".format(e - s)
    println("=================Points Count=================")
    println(s"$msg (in $t ms)")
    println("==============================================")
    result
  }

  def main(args: Array[String]): Unit = {
    val input = new Path(args.head)
    val chunkPath = System.getProperty("user.dir") + "/chunks/"
    //val chunkPath = "/mnt1/tmp/"

    val conf = new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("PointCloudCount")
      .set("spark.serializer", classOf[KryoSerializer].getName)
      .set("spark.kryo.registrator", classOf[KryoRegistrator].getName)

    implicit val sc = new SparkContext(conf)

    /*val start = System.currentTimeMillis

    val pipeline = Pipeline(fileToPipelineJson(new File(args.head)).toString)

    // PDAL itself is not 100% threadsafe
    timed("pipeline.execute") { pipeline.execute }

    val pointViewIterator = pipeline.getPointViews()
    // conversion to list to load everything into JVM memory
    val packedPoints = pointViewIterator.map { pointView =>
      val packedPoint = timed("pointView.getPointCloud") { pointView.getPointCloud }

      pointView.dispose()
      packedPoint
    }

    val pointsCount = packedPoints.map { pointCloud =>
      var acc = 0l
      cfor(0)(_ < pointCloud.length, _ + 1) { i =>
        pointCloud.get(i)
        acc += 1
      }
      acc
    }.sum

    val end = System.currentTimeMillis

    val time = "%,d".format(end - start)
    println("=================Points Count=================")
    println(s"pointsCount (in $time ms): ${pointsCount}")
    println("==============================================")*/

    /*val pointClouds =
      time("Read point clouds") {
        val pipeline = Pipeline(fileToPipelineJson(new java.io.File(args.head)).toString)
        pipeline.execute
        val pointViewIterator = pipeline.pointViews()
        val result =
          pointViewIterator.toList.map { pointView =>
            val pointCloud =
              pointView.getPointCloud(
                metadata = pipeline.getMetadata(),
                schema   = pipeline.getSchema()
              )

            pointView.dispose()
            pointCloud
          }.toIterator
        pointViewIterator.dispose()
        pipeline.dispose()
        result
      }

    val pointCloud = pointClouds.next

    val (extent, (min, max)) =
      time("Finding min and max heights") {
        var (xmin, xmax) = (Double.MaxValue, Double.MinValue)
        var (ymin, ymax) = (Double.MaxValue, Double.MinValue)
        var (zmin, zmax) = (Double.MaxValue, Double.MinValue)
        cfor(0)(_ < pointCloud.length, _ + 1) { i =>
          val x = pointCloud.getX(i)
          val y = pointCloud.getY(i)
          val z = pointCloud.getZ(i)

          if(x < xmin) xmin = x
          if(x > xmax) xmax = x
          if(y < ymin) ymin = y
          if(y > ymax) ymax = y
          if(z < zmin) zmin = z
          if(z > zmax) zmax = z
        }
        (Extent(xmin, ymin, xmax, ymax), (zmin, zmax))
      }

    println(s"MIN, MAX = ($min, $max)")*/


    /*val source = time("Read point clouds") { HadoopPointCloudRDD(input) }

    val result: List[(Extent, (Double, Double))] = time("Finding min and max heights") {
      source.mapPartitions {
        _.map { case (_, pointCloud) =>
          var (xmin, xmax) = (Double.MaxValue, Double.MinValue)
          var (ymin, ymax) = (Double.MaxValue, Double.MinValue)
          var (zmin, zmax) = (Double.MaxValue, Double.MinValue)
          cfor(0)(_ < pointCloud.length, _ + 1) { i =>
            val x = time("x") { pointCloud.getX(i) }
            val y = time("y") { pointCloud.getY(i) }
            val z = time("z") { pointCloud.getZ(i) }

            if (x < xmin) xmin = x
            if (x > xmax) xmax = x
            if (y < ymin) ymin = y
            if (y > ymax) ymax = y
            if (z < zmin) zmin = z
            if (z > zmax) zmax = z
          }
          (Extent(xmin, ymin, xmax, ymax), (zmin, zmax))
        }
      }.collect().toList
    }

    val (extent, (min, max)) = result.head

    println(s"MIN, MAX = ($min, $max)")*/

    //os.environ['PWD'], "chunk-temp")


    try {

      val source = HadoopPointCloudRDD(
        input,
        HadoopPointCloudRDD.Options(tmpDir = Some(chunkPath))
      )

      val points = source.flatMap(_._2).cache

      /*val extent = source.map(_._1.projectedExtent3D.extent).reduce(_ combine _)
      val ld = LayoutDefinition(
        extent,
        TileLayout(layoutCols = 5, layoutRows = 5, tileCols = 10, tileRows = 10)
      )

      val tiled = points.tileToLayout(ld).cache


      val start = System.currentTimeMillis
      val pointsCount = tiled.mapPartitions { _.map { case (_, pointCloud) =>
        var acc = 0l
        cfor(0)(_ < pointCloud.length, _ + 1) { i =>
          pointCloud.get(i)
          acc += 1
        }
        acc
      } }.reduce(_ + _)

      /*val pointsCountz = points.mapPartitions { _.flatMap { pointCloud =>
        val length = pointCloud.length
        val factor = 10

        val portion = length / factor
        val lastPortion = length - portion * (factor - 1)

        val pointClouds = new Array[PointCloud](factor)

        var z = 0
        cfor(0)(_ < factor, _ + 1) { k =>
          val currentPortion = if(k == factor - 1) lastPortion else portion
          val arr = new Array[Array[Byte]](currentPortion)

          val cmp = if(k == factor - 1) portion * k + lastPortion else portion * (k + 1)

          var zz = 0
          while(z < cmp) {
            arr(zz) = pointCloud.get(z)
            zz += 1
            z += 1
          }

          pointClouds(k) = PointCloud(arr.flatten, pointCloud.dimTypes)
        }

        pointClouds.toIterator*/
        /*


        //val arr = new Array[Int](pointCloud.length)
        var acc = 0l
        cfor(0)(_ < pointCloud.length, _ + 1) { i =>
          pointCloud.get(i)
          //val s = System.currentTimeMillis
          //pointCloud.get(i)
          //val e = System.currentTimeMillis
          //val t = "%,d".format(e - s).toInt
          //arr(i) = t
          acc += 1
        }
        acc
        //arr*/
    //} }.repartition(38 * 10).cache // repartition each file splits into 10 additional chunks */

      val start = System.currentTimeMillis
      val pointsCount = points.mapPartitions { _.map { pointCloud =>

        var acc = 0l
        //timed("cfor") {
          cfor(0)(_ < pointCloud.length, _ + 1) { i =>
            pointCloud.get(i)
            //val s = System.currentTimeMillis
            //pointCloud.get(i)
            //val e = System.currentTimeMillis
            //val t = "%,d".format(e - s).toInt
            //arr(i) = t
            acc += 1
          }
        //}
        acc
        //arr*/
      } }.reduce(_ + _)
      val end = System.currentTimeMillis

      val time = "%,d".format(end - start)
      println("=================Points Count=================")
      println(s"pointsCount (in $time ms): ${pointsCount}")
      println("==============================================")
    } finally sc.stop()
  }
}
