package com.busymachines.commons

import com.typesafe.config._
import java.net.URL
import com.busymachines.commons.implicits._
import scala.math.Ordering.Implicits._
import java.util.List
import java.util.Set
import java.lang.{Boolean, Double, Long}
import java.util.Map.Entry
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import java.io.File

object CommonConfigFactory {
  private[commons] var usedPaths = TrieMap[String, Unit]()
  val configFiles = System.getProperty("config") match {
    case null => Nil
    case files => files.split(",").toList
  }
  val defaultConfig = ConfigFactory.load(getClass.getClassLoader)
  val fileConfigs = configFiles.map(config => ConfigFactory.parseFile(new File(config)))
  val config = fileConfigs.foldRight(defaultConfig)((config, defaultConfig) => config.withFallback(defaultConfig))
}

object CommonConfig extends CommonConfig("") with Logging {

  val devmode = booleanOption("devmode") getOrElse false
  
  if (devmode) {
    info("Starting in development mode.")
  }

  import scala.collection.JavaConversions._

  override def mkString(sep: String) =
    toStringSeq.mkString(sep)

  def toStringSeq: Seq[String] =
  toStringSeq2.map(x => x._1 + "=" + x._2).sorted.distinct

  private def toStringSeq2: Seq[(String, String)] =
    CommonConfigFactory.usedPaths.keys.filter(_.nonEmpty).filter(CommonConfigFactory.config.hasPath).toSeq.flatMap { path =>
        CommonConfigFactory.config.getValue(path) match {
          case config: ConfigObject => toStringSeq4(config, path)
          case other => Seq.empty
        }
    }

  private def toStringSeq4(value: ConfigValue, path: String) : Seq[(String, String)] =
    value match {
      case config: ConfigObject => config.entrySet.toSeq.flatMap { e =>
        toStringSeq4(e.getValue, path + "." + e.getKey.replace("\"", ""))
      }
      case list: ConfigList => Seq((path, list.map(_.render).mkString("[", ",", "]")))
      case value => Seq((path, value.render))
    }
}

class RichCommonConfigType[A <: CommonConfig](f: String => A) {

  /**
   * Create a sequence of top-level config objects.
   */
  def seq(baseName : String): Seq[A] = {
    val result = ArrayBuffer[A]()
    var foundMore = true
    var index = 0
    val config = CommonConfigFactory.config(baseName).theConfig
    while (foundMore) {
      if (config.hasPath(index.toString)) {
        result += f(baseName + "." + index.toString)
        index += 1
      } else {
        foundMore = false
      }
    }
    result.toSeq
  }
}

class CommonConfig(baseName: String) extends RichConfig(CommonConfigFactory.config(baseName).theConfig) {
  CommonConfigFactory.usedPaths += (baseName -> ())

  def config[A <: CommonConfig](f: String => A, name : String): A = {
    f(baseName + "." + name)
  }

  def seq[A <: CommonConfig](f: String => A, name : String): Seq[A] = {
    val result = ArrayBuffer[A]()
    var foundMore = true
    var index = 0
    val config = CommonConfigFactory.config(baseName + "." + name).theConfig
    while (foundMore) {
      if (config.hasPath(index.toString)) {
        result += f(baseName + "." + name + "." + index.toString)
        index += 1
      } else {
        foundMore = false
      }
    }
    result.toSeq
  }
}
