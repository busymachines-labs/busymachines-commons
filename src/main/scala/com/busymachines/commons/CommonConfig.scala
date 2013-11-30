package com.busymachines.commons

import com.typesafe.config.ConfigFactory
import java.net.URL
import com.busymachines.commons.implicits._
import com.typesafe.config.Config
import scala.collection.SeqProxy

object CommonConfigFactory {
  val configFiles = System.getProperty("config") match {
    case null => Nil
    case files => files.split(",").toList
  }
  val defaultConfig = ConfigFactory.load(getClass.getClassLoader)
  val fileConfigs = configFiles.map(config => ConfigFactory.parseURL(new URL(config)))
  val config = fileConfigs.foldRight(defaultConfig)((config, defaultConfig) => config.withFallback(defaultConfig))  
}

object CommonConfig extends CommonConfig("") with Logging {

  val devmode = booleanOption("busymachines.devmode") getOrElse false
  
  if (devmode) {
    info("Starting in development mode.")
  }
}

class CommonConfig(config : Config) extends RichConfig(config) {
  def this(baseName : String) = this(CommonConfigFactory.config(baseName).theConfig)
}

