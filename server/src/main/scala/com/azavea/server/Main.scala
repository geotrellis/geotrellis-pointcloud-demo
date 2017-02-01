package com.azavea.server

import geotrellis.spark.io.kryo.KryoRegistrator
import geotrellis.spark.io.hadoop._
import geotrellis.util._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer

import scala.concurrent.ExecutionContext.Implicits.global

object AkkaSystem {
  implicit val system = ActorSystem("geotrellis-pointcloud-demo")
  implicit val materializer = ActorMaterializer()

  trait LoggerExecutor {
    protected implicit val log = Logging(system, "app")
  }
}

object Main extends Router with Config {
  import AkkaSystem._

  val conf = new SparkConf()
    .setIfMissing("spark.master", "local[*]")
    .setAppName("GeoTrellis PointCloud Server")
    .set("spark.serializer", classOf[KryoSerializer].getName)
    .set("spark.kryo.registrator", classOf[KryoRegistrator].getName)
    .set("spark.kryoserializer.buffer.max", "1g")
    .set("spark.kryoserializer.buffer", "1g")

  implicit val sc = new SparkContext(conf)

  lazy val attributeStore = HadoopAttributeStore(attributeStorePath, sc.hadoopConfiguration)
  lazy val tileReader = HadoopValueReader(attributeStore)
  lazy val layerReader = HadoopLayerReader(attributeStore)

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(routes, httpConfig.interface, httpConfig.port)
  }
}
