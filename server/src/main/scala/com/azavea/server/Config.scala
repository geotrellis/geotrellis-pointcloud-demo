package com.azavea.server

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus
import net.ceedubs.ficus.readers.ArbitraryTypeReader

trait Config {
  import ArbitraryTypeReader._
  import Ficus._

  protected case class HttpConfig(interface: String, port: Int)

  private val config = ConfigFactory.load()
  protected val httpConfig = config.as[HttpConfig]("http")
  val staticPath = config.as[String]("server.static-path")
  val attributeStorePath = config.as[String]("server.attribute-store-path")
}
