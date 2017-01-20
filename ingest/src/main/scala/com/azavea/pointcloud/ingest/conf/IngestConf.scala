package com.azavea.pointcloud.ingest.conf

import geotrellis.raster.CellSize
import geotrellis.vector.Extent

object IngestConf {
  case class Options(
    inputPath: String = "file:///data/test/",
    catalogPath: String = "file:///data/catalog2",
    testOutput: String = "/data/idw-ingest-out.tif",
    layerName: String = "elevation",
    persist: Boolean = false,
    pyramid: Boolean = false,
    zoomed: Boolean = false,
    cellSize: CellSize = CellSize(0.5, 0.5),
    numPartitions: Int = 5000,
    minZoom: Int = 7,
    maxValue: Int = 400,
    destCrs: String = "EPSG:3857",
    extent: Option[Extent] = None,
    inputCrs: Option[String] = None
  )

  val help = """
               |geotrellis-pointcloud-ingest
               |
               |Usage: geotrellis-pointcloud-ingest [options]
               |
               |  --inputPath <value>
               |        inputPath is a non-empty String property [default: file:///data/test/]
               |  --catalogPath <value>
               |        catalogPath is a non-empty String property [default: file:///data/catalog2]
               |  --layerName <value>
               |        layerName is a non-empty String property [default: elevation]
               |  --extent <value>
               |        extent is a non-empty String in LatLng
               |  --inputCrs <value>
               |        inputCrs is a non-empty String
               |  --destCrs <value>
               |        destCrs is a non-empty String [default: EPSG:3857]
               |  --persist <value>
               |        persist is a a boolean option [default: false]
               |  --pyramid <value>
               |        pyramid is a boolean option [default: false]
               |  --zoomed <value>
               |        zoomed is a boolean option [default: false]
               |  --cellSize <value>
               |        cellSize is a non-empty String [default: 0.5,0.5]
               |  --numPartitions <value>
               |        numPatition is an integer value [default: 5000]
               |  --minZoom <value>
               |        minZoom is an integer value [default: 7]
               |  --maxValue <value>
               |        maxValue is an integer value [default: 400]
               |  --help
               |        prints this usage text
             """.stripMargin

  def nextOption(opts: Options, list: Seq[String]): Options = {
    list.toList match {
      case Nil => opts
      case "--inputPath" :: value :: tail =>
        nextOption(opts.copy(inputPath = value), tail)
      case "--catalogPath" :: value :: tail =>
        nextOption(opts.copy(catalogPath = value), tail)
      case "--layerName" :: value :: tail =>
        nextOption(opts.copy(layerName = value), tail)
      case "--extent" :: value :: tail =>
        nextOption(opts.copy(extent = Some(Extent.fromString(value))), tail)
      case "--inputCrs" :: value :: tail =>
        nextOption(opts.copy(inputCrs = Some(value)), tail)
      case "--destCrs" :: value :: tail =>
        nextOption(opts.copy(destCrs = value), tail)
      case "--pyramid" :: value :: tail =>
        nextOption(opts.copy(pyramid = value.toBoolean), tail)
      case "--zoomed" :: value :: tail =>
        nextOption(opts.copy(zoomed = value.toBoolean), tail)
      case "--cellSize" :: value :: tail =>
        nextOption(opts.copy(cellSize = CellSize.fromString(value)), tail)
      case "--numPartitions" :: value :: tail =>
        nextOption(opts.copy(numPartitions = value.toInt), tail)
      case "--minZoom" :: value :: tail =>
        nextOption(opts.copy(minZoom = value.toInt), tail)
      case "--maxValue" :: value :: tail =>
        nextOption(opts.copy(maxValue = value.toInt), tail)
      case "--help" :: tail => {
        println(help)
        sys.exit(1)
      }
      case option :: tail => {
        println(s"Unknown option ${option}")
        println(help)
        sys.exit(1)
      }
    }
  }

  def parse(args: Seq[String]) = nextOption(Options(), args)

  def apply(args: Seq[String]): Options = parse(args)
}
