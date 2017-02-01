package com.azavea.server

import geotrellis.raster.Tile
import geotrellis.spark.SpatialKey
import geotrellis.spark.util.cache.LRUCache
import com.typesafe.config.ConfigFactory

trait CacheSupport {
  val cachedTiles = new LRUCache[SpatialKey, Tile](
    maxSize = ConfigFactory.load().getLong("server.tile-cache"),
    sizeOf  = {_ => 1l}
  )
}
