package com.busymachines.commons

import com.typesafe.config.ConfigFactory
import java.net.URL
import com.busymachines.commons.implicits._

object CommonConfig {
  val configFiles = System.getProperty("config") match {
    case null => Nil
    case files => files.split(",").toList
  }
  val defaultConfig = ConfigFactory.load(getClass.getClassLoader)
  val fileConfigs = configFiles.map(config => ConfigFactory.parseURL(new URL(config)))
  val config = fileConfigs.foldRight(defaultConfig)((config, defaultConfig) => config.withFallback(defaultConfig))

  val devmode = config.getBooleanOption("busymachines.devmode") getOrElse false
}

class CommonConfig(baseName : String) {
  val globalConfig = CommonConfig.config
  lazy val config = globalConfig.getConfigOrEmpty(baseName) 
}