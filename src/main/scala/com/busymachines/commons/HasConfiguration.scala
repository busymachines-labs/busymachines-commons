package com.busymachines.commons

import com.busymachines.commons.implicits._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.net.URL

object HasConfiguration {
  val configFiles = System.getProperty("config") match {
    case null => Nil
    case files => files.split(",").toList
  }
  val defaultConfig = ConfigFactory.load(getClass.getClassLoader)
  val fileConfigs = configFiles.map(config => ConfigFactory.parseURL(new URL(config)))
  val globalConfig = fileConfigs.foldRight(defaultConfig)((config, defaultConfig) => config.withFallback(defaultConfig))
}

trait HasConfiguration {
  val globalConfig = HasConfiguration.globalConfig
  val configBaseName : Option[String] = None
  val actualConfigBaseName = configBaseName getOrElse getClass.getPackage.getName
  lazy val config = globalConfig.getConfigOption(actualConfigBaseName) getOrElse ConfigFactory.empty
}