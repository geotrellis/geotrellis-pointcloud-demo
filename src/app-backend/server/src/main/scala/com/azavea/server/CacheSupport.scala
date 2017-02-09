package com.azavea.server

import geotrellis.raster.Tile
import geotrellis.spark.SpatialKey
import geotrellis.spark.util.cache.LRUCache
import com.typesafe.config.ConfigFactory

trait CacheSupport {
  // TODO: Move to   "com.github.blemale" %% "scaffeine" % "2.0.0",
  // use layer id as well
  val cachedTiles = new LRUCache[SpatialKey, Tile](
    maxSize = ConfigFactory.load().getLong("server.tile-cache"),
    sizeOf  = {_ => 1l}
  )
}
