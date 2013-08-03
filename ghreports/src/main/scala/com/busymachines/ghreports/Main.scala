package com.busymachines.ghreports

import com.typesafe.config.ConfigFactory
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import scala.concurrent.Future
import akka.io.IO
import spray.can.Http
import spray.can.Http.Bind

object Main extends App with Logging {

  // Startup
  val config = ConfigFactory.load
  info("Configuration: \n  " + config.toSeq.filter(_.startsWith("com.kentivo.mdm")).mkString("\n  ") + "\n")
  debug("Full Configuration: \n  " + config.mkString("\n  ") + "\n")
  info(s"Java Heap Size: ${Runtime.getRuntime.maxMemory / 1000000} MB")

  // Start UI server
  val assembly = new SystemAssembly
  assembly.server.start
}