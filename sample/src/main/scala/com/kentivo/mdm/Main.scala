package com.kentivo.mdm

import com.typesafe.config.ConfigFactory
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import com.kentivo.mdm.logic.IcecatAdapter
import scala.concurrent.Future
import com.kentivo.mdm.domain.Repository

object Main extends App with Logging {

  // Startup
  val config = ConfigFactory.load
  info("Configuration: \n  " + config.toSeq.filter(_.startsWith("com.kentivo.mdm")).mkString("\n  ") + "\n")
  debug("Full Configuration: \n  " + config.mkString("\n  ") + "\n")
  info(s"Java Heap Size: ${Runtime.getRuntime.maxMemory / 1000000} MB")

  // Start UI server
  val assembly = new SystemAssembly
//  assembly.server.start
  
  import assembly.executionContext
  import assembly.actorSystem
//  val icecat = new IcecatAdapter(assembly.itemDao, assembly.mediaDao)
//  icecat.importAll
  assembly.server.start
}