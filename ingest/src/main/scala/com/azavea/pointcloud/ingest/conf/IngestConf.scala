package com.azavea.pointcloud.ingest.conf

import com.azavea.annotation.GenParser

import geotrellis.raster.CellSize
import geotrellis.vector.Extent

@GenParser("geotrellis-pointcloud-ingest", requiredFields = "inputPath", "catalogPath")
case class IngestConf(
  inputPath: String = "/data/test/",
  catalogPath: String = "/data/catalog",
  layerName: String = "elevation",
  persist: Boolean = true,
  pyramid: Boolean = true,
  zoomed: Boolean = true,
  cellSize: CellSize = CellSize(0.5, 0.5),
  numPartitions: Int = 5000,
  minZoom: Int = 7,
  maxZoom: Option[Int] = None,
  maxValue: Option[Int] = None,
  destCrs: String = "EPSG:3857",
  extent: Option[Extent] = None,
  inputCrs: Option[String] = None,
  testOutput: Option[String] = None
)

object IngestConf {
  implicit def stringToExtent(str: String): Extent = Extent.fromString(str)
  implicit def stringToCellSize(str: String): CellSize = CellSize.fromString(str)
  implicit def liftStringToOptionT[T](str: String)(implicit ev: String => T): Option[T] = Some(str)
}
