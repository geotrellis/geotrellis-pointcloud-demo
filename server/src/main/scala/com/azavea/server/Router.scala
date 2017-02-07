package com.azavea.server

import com.azavea.server.mapalgebra.focal._

import geotrellis.raster._
import geotrellis.raster.io._
import geotrellis.raster.histogram.{Histogram, StreamingHistogram}
import geotrellis.raster.render._
import geotrellis.spark._
import geotrellis.spark.buffer.BufferedTile
import geotrellis.pointcloud.spark._
import geotrellis.pointcloud.spark.io._
import geotrellis.pointcloud.spark.io.hadoop._
import geotrellis.spark.io._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.tiling.FloatingLayoutScheme
import geotrellis.spark.mapalgebra.focal._
import geotrellis.spark.mapalgebra.focal.hillshade._
import geotrellis.proj4._
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.raster.mapalgebra.focal.{Square, TargetCell}
import geotrellis.raster.mapalgebra.focal.hillshade.Hillshade
import geotrellis.spark.io.AttributeStore
import geotrellis.spark.io.avro.AvroRecordCodec
import geotrellis.util._
import geotrellis.vector._
import geotrellis.vector.io._

import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import akka.http.scaladsl.server.Directives
import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spire.syntax.cfor._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

trait Router extends Directives with CacheSupport with AkkaSystem.LoggerExecutor {
  val conf: SparkConf
  val tileReader: ValueReader[LayerId]
  val layerReader: LayerReader[LayerId]
  val attributeStore: AttributeStore
  val staticPath: String

  implicit val sc: SparkContext

  import AkkaSystem.materializer

  def seqFutures[T, U](items: TraversableOnce[T])(func: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) {
      (f, item) => f.flatMap {
        x => func(item).map(_ :: x)
      }
    } map (_.reverse)
  }

  def populateKeys[K: SpatialComponent](key: K): Seq[K] = {
    val SpatialKey(c, r) = key.getComponent[SpatialKey]

    Seq(
      key.setComponent(SpatialKey(c - 1, r + 1)),
      key.setComponent(SpatialKey(c, r + 1)),
      key.setComponent(SpatialKey(c + 1, r + 1)),
      key.setComponent(SpatialKey(c - 1, r)),
      key.setComponent(SpatialKey(c, r)),
      key.setComponent(SpatialKey(c + 1, r)),
      key.setComponent(SpatialKey(c - 1, r - 1)),
      key.setComponent(SpatialKey(c, r - 1)),
      key.setComponent(SpatialKey(c + 1, r - 1))
    )
  }

  def keyToBounds[K: SpatialComponent](key: K): KeyBounds[K] = {
    val SpatialKey(c, r) = key.getComponent[SpatialKey]

    KeyBounds(
      key.setComponent(SpatialKey(c - 1, r - 1)),
      key.setComponent(SpatialKey(c + 1, r + 1))
    )
  }

  def readTileNeighbours[K: SpatialComponent: AvroRecordCodec: JsonFormat: ClassTag](layerId: LayerId, key: K): Future[Seq[(K, Tile)]] = {
    Future.sequence(populateKeys(key).map { k => Future {
        try {
          Some(k -> cachedTiles.getOrInsert(k.getComponent[SpatialKey], tileReader.reader[K, Tile](layerId).read(k)))
        } catch {
          case e: ValueNotFoundError => None
        }
    } }) map (_.flatten)
  }

  def focalCompositeTileApply[
    K: SpatialComponent: AvroRecordCodec: JsonFormat: ClassTag
  ](layerId: LayerId, key: K, colorRamp: String)(f: Seq[(K, Tile)] => Tile) =
    readTileNeighbours(layerId, key) map { tileSeq => f(tileSeq) }

  def DIMRender(tile: Tile, layerId: LayerId, colorRamp: String): HttpResponse = {
    val breaks =
      attributeStore
        .read[Histogram[Double]](LayerId(layerId.name, 0), "histogram")
        .asInstanceOf[StreamingHistogram]
        .quantileBreaks(50)

    val ramp =
      ColorRampMap
        .getOrElse(colorRamp, ColorRamps.BlueToRed)
    val colorMap =
      ramp
        .toColorMap(breaks, ColorMap.Options(fallbackColor = ramp.colors.last))

    val bytes = tile.renderPng(colorMap)

    HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`image/png`), bytes))
  }

  def index(i: Option[Int] = None) = i match {
    case Some(n) if n > 1 && n < 6 => s"/index${n}.html"
    case _ => "/index.html"
  }

  def routes =
    pathPrefix("ping") {
      get {
        complete { "pong" }
      }
    } ~
    pathEndOrSingleSlash {
      parameter('n.as[Int] ?) { n =>
        getFromFile(staticPath + index(n))
      }
    } ~
    pathPrefix("") {
      getFromDirectory(staticPath)
    } ~
    pathPrefix("gt") {
      path("colors")(complete(ColorRampMap.getJson))
    } ~
      pathPrefix("tms") {
        pathPrefix("hillshade") {
          pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layerName, zoom, x, y) =>
            parameters('colorRamp ? "blue-to-red", 'azimuth.as[Double] ? 315, 'altitude.as[Double] ? 45, 'zFactor.as[Double] ? 1, 'targetCell ? "all") { (colorRamp, azimuth, altitude, zFactor, targetCell) =>
              val target = targetCell match {
                case "nodata" => TargetCell.NoData
                case "data" => TargetCell.Data
                case _ => TargetCell.All
              }
              val layerId = LayerId(layerName, zoom)
              val key = SpatialKey(x, y)
              val keys = populateKeys(key)
              val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

              complete {
                readTileNeighbours(layerId, key) map { tileSeq =>
                  ContextCollection(tileSeq, md).hillshade(azimuth, altitude, zFactor, target)
                    .find {
                      _._1 == key
                    }
                    .map(_._2)
                    .map {
                      DIMRender(_, layerId, colorRamp)
                    }
                }
              }
            }
          }
        } ~
          pathPrefix("hillshade-buffered") {
            pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layerName, zoom, x, y) =>
              parameters('colorRamp ? "blue-to-red", 'azimuth.as[Double] ? 315, 'altitude.as[Double] ? 45, 'zFactor.as[Double] ? 1, 'targetCell ? "all") { (colorRamp, azimuth, altitude, zFactor, targetCell) =>
                val target = targetCell match {
                  case "nodata" => TargetCell.NoData
                  case "data" => TargetCell.Data
                  case _ => TargetCell.All
                }
                val layerId = LayerId(layerName, zoom)
                val key = SpatialKey(x, y)
                val keys = populateKeys(key)
                val kb = keyToBounds(key)
                val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

                complete {
                  readTileNeighbours(layerId, key) map {
                    _.runOnSeq(key, md) { case (tile, bounds) =>
                      Hillshade(tile, Square(1), bounds, md.cellSize, azimuth, altitude, zFactor, target)
                    }
                  } map {
                    DIMRender(_, layerId, colorRamp)
                  }
                }
              }
            }
          } ~
          pathPrefix("hillshade-rdd") {
            pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layerName, zoom, x, y) =>
              parameters('colorRamp ? "blue-to-red", 'azimuth.as[Double] ? 315, 'altitude.as[Double] ? 45, 'zFactor.as[Double] ? 1, 'targetCell ? "all") { (colorRamp, azimuth, altitude, zFactor, targetCell) =>
                val target = targetCell match {
                  case "nodata" => TargetCell.NoData
                  case "data" => TargetCell.Data
                  case _ => TargetCell.All
                }
                val layerId = LayerId(layerName, zoom)
                val key = SpatialKey(x, y)
                val keys = populateKeys(key)
                val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

                complete {
                  readTileNeighbours(layerId, key) map { tileSeq =>
                    ContextRDD(sc.parallelize(tileSeq), md).hillshade(azimuth, altitude, zFactor, target)
                      .lookup(key)
                      .headOption
                      .map {
                        DIMRender(_, layerId, colorRamp)
                      }
                  }
                }
              }
            }
          } ~
          pathPrefix("tiff") {
            pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layerName, zoom, x, y) =>
              val layerId = LayerId(layerName, zoom)
              val key = SpatialKey(x, y)
              val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

              complete {
                Future {
                  val tileOpt =
                    try {
                      Some(tileReader.reader[SpatialKey, Tile](layerId).read(key))
                    } catch {
                      case e: ValueNotFoundError =>
                        None
                    }
                  tileOpt.map { tile =>
                    val extent = md.mapTransform(key)
                    val geotiff = GeoTiff(tile, extent, WebMercator)
                    val bytes = GeoTiffWriter.write(geotiff)

                    HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`image/tiff`), bytes))
                  }
                }
              }
            }
          } ~
          pathPrefix("png") {
            pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layerName, zoom, x, y) =>
              parameters('colorRamp ? "blue-to-red") { colorRamp =>
                val layerId = LayerId(layerName, zoom)
                val key = SpatialKey(x, y)
                val md = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId)

                println(key)

                complete {
                  Future {
                    val tileOpt =
                      try {
                        Some(tileReader.reader[SpatialKey, Tile](layerId).read(key))
                      } catch {
                        case e: ValueNotFoundError =>
                          None
                      }
                    tileOpt.map {
                      DIMRender(_, layerId, colorRamp)
                    }
                  }
                }
              }
            }
          } ~
          pathPrefix("diff-tms") {
            pathPrefix("png") {
              pathPrefix(Segment / Segment / IntNumber / IntNumber / IntNumber) { (layerName1, layerName2, zoom, x, y) =>
                parameters('colorRamp ? "green-to-red", 'breaks ? "-11,-10,-3,-4,-5,-6,-2,-1,-0.1,-0.06,-0.041,-0.035,-0.03,-0.025,-0.02,-0.019,-0.017,-0.015,-0.01,-0.008,-0.002,0.002,0.004,0.006,0.009,0.01,0.013,0.015,0.027,0.04,0.054,0.067,0.1,0.12,0.15,0.23,0.29,0.44,0.66,0.7,1,1.2,1.4,1.6,1.7,2,3,4,5,50,60,70,80,90,150,200") { (colorRamp, pbreaks) =>
                  val (layerId1, layerId2) = LayerId(layerName1, zoom) -> LayerId(layerName2, zoom)
                  val key = SpatialKey(x, y)
                  val (md1, md2) = attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId1) -> attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId2)

                  complete {
                    Future {
                      val tileOpt =
                        try {
                          val tile1 = tileReader.reader[SpatialKey, Tile](layerId1).read(key)
                          val tile2 = tileReader.reader[SpatialKey, Tile](layerId2).read(key)

                          val diff = tile1 - tile2

                          Some(diff)
                        } catch {
                          case e: ValueNotFoundError =>
                            None
                        }
                      tileOpt.map { tile =>
                        println(s"tile.findMinMaxDouble: ${tile.findMinMaxDouble}")

                        println(s"pbreaks: ${pbreaks}")

                        /*val l1 = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName1, 19))
                  val l2 = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName2, 19))

                  println(s"zzzz: ${(l1 - l2).histogram(512).asInstanceOf[StreamingHistogram].quantileBreaks(50).toList}")*/

                        val breaks = pbreaks match {
                          case "none" => {
                            tile
                              .histogramDouble
                              .asInstanceOf[StreamingHistogram]
                              .quantileBreaks(50)
                          }
                          case s => s.split(",").map(_.toDouble)
                        }

                        println(s"breaks: ${breaks.toList}")

                        val ramp =
                          ColorRampMap
                            .getOrElse(colorRamp, ColorRamps.BlueToRed)
                        val colorMap =
                          ramp
                            .toColorMap(breaks, ColorMap.Options(fallbackColor = ramp.colors.last))

                        //val bytes = tile.renderPng(colorMap)
                        val bytes = tile.renderPng(ColorRampMap.gr)

                        HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`image/png`), bytes))
                      }
                    }
                  }
                }
              }
            }
          } ~
          pathPrefix("diff2-tms") {
            pathPrefix("png") {
              pathPrefix(Segment / Segment / Segment / Segment / IntNumber / IntNumber / IntNumber) { (layerName1, layerName2, layerName3, layerName4, zoom, x, y) =>
                parameters('colorRamp ? "green-to-red", 'breaks ? "-11,-10,-3,-4,-5,-6,-2,-1,-0.1,-0.06,-0.041,-0.035,-0.03,-0.025,-0.02,-0.019,-0.017,-0.015,-0.01,-0.008,-0.002,0.002,0.004,0.006,0.009,0.01,0.013,0.015,0.027,0.04,0.054,0.067,0.1,0.12,0.15,0.23,0.29,0.44,0.66,0.7,1,1.2,1.4,1.6,1.7,2,3,4,5,50,60,70,80,90,150,200") { (colorRamp, pbreaks) =>

                  val (layerId1, layerId2, layerId3, layerId4) = (LayerId(layerName1, zoom), LayerId(layerName2, zoom), LayerId(layerName3, zoom), LayerId(layerName4, zoom))
                  val key = SpatialKey(x, y)
                  val (md1, md2, md3, md4) = (attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId1), attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId2), attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId3), attributeStore.readMetadata[TileLayerMetadata[SpatialKey]](layerId4))

                  complete {
                    Future {
                      val tileOpt =
                        try {
                          val tile1 = tileReader.reader[SpatialKey, Tile](layerId1).read(key)
                          val tile2 = tileReader.reader[SpatialKey, Tile](layerId2).read(key)
                          val tile3 = tileReader.reader[SpatialKey, Tile](layerId3).read(key)
                          val tile4 = tileReader.reader[SpatialKey, Tile](layerId4).read(key)

                          val diff = (tile1 - tile2) - (tile3 - tile4)

                          Some(diff)
                        } catch {
                          case e: ValueNotFoundError =>
                            None
                        }
                      tileOpt.map { tile =>
                        println(s"tile.findMinMaxDouble: ${tile.findMinMaxDouble}")

                        println(s"pbreaks: ${pbreaks}")

                        /*val l1 = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName1, 19))
                    val l2 = layerReader.read[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](LayerId(layerName2, 19))

                    println(s"zzzz: ${(l1 - l2).histogram(512).asInstanceOf[StreamingHistogram].quantileBreaks(50).toList}")*/

                        val breaks = pbreaks match {
                          case "none" => {
                            tile
                              .histogramDouble
                              .asInstanceOf[StreamingHistogram]
                              .quantileBreaks(50)
                          }
                          case s => s.split(",").map(_.toDouble)
                        }

                        println(s"breaks: ${breaks.toList}")

                        val ramp =
                          ColorRampMap
                            .getOrElse(colorRamp, ColorRamps.BlueToRed)
                        val colorMap =
                          ramp
                            .toColorMap(breaks, ColorMap.Options(fallbackColor = ramp.colors.last))

                        val bytes = tile.renderPng(colorMap)

                        HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`image/png`), bytes))
                      }
                    }
                  }
                }
              }
            }
          }
      }

  def time[T](msg: String)(f: => T): (T, JsObject) = {
    val s = System.currentTimeMillis
    val result = f
    val e = System.currentTimeMillis
    val t = "%,d".format(e - s)
    val obj = JsObject(
      "TIMING RESULT" -> JsObject(
        "msg"          -> msg.toJson,
        "time (in ms)" -> t.toJson
      )
    )

    println(obj.toString)

    result -> obj
  }
}
