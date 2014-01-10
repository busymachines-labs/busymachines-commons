package com.busymachines.commons

import com.typesafe.config._
import java.net.URL
import com.busymachines.commons.implicits._
import scala.math.Ordering.Implicits._
import java.util
import java.lang.{Boolean, Double, Long}
import java.util.Map.Entry
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

object CommonConfigFactory {
  var usedPaths = TrieMap[String, Unit]()
  val configFiles = System.getProperty("config") match {
    case null => Nil
    case files => files.split(",").toList
  }
  val defaultConfig = ConfigFactory.load(getClass.getClassLoader)
  val fileConfigs = configFiles.map(config => ConfigFactory.parseURL(new URL(config)))
  val config = fileConfigs.foldRight(defaultConfig)((config, defaultConfig) => config.withFallback(defaultConfig))
}

object CommonConfig extends CommonConfig("") with Logging {

  val devmode = booleanOption("devmode") getOrElse false
  
  if (devmode) {
    info("Starting in development mode.")
  }

  import scala.collection.JavaConversions._

  override def mkString(sep: String) =
    toSeq.sortWith(_ < _).map(x => x._1 + "=" + x._2).mkString(sep)

  def toSeq : Seq[(String, String)] = {
    println("Paths: " + CommonConfigFactory.usedPaths)
    CommonConfigFactory.usedPaths.keys.filter(_.nonEmpty).filter(CommonConfigFactory.config.hasPath).toSeq.flatMap { path =>
      CommonConfigFactory.config.getValue(path) match {
          case config: Config => super.toSeq(path, config)
          case config: ConfigObject => super.toSeq(path, config.toConfig)
        //        case config: Config => super.toSeq(path, config)
        case value =>
          println(value.getClass)
          Seq((path.replace("\"", ""), "Q:"+value.toString))
      }
    }
    }
}

class RichCommonConfigType[A <: CommonConfig](f: String => A) {
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
}

class RichCommonConfig[A, T <: String => CommonConfig](config: T) {
}

class CommonConfig2(config : Config) extends RichConfig(new ConfigDelegate(config)) {
  def this(baseName : String) = this(CommonConfigFactory.config(baseName).theConfig)
}

class ConfigDelegate(delegate: Config) extends Config {

  def p(path: String) = {
    CommonConfigFactory.usedPaths += (path -> ())
    path
  }

  def root() = delegate.root()

  def origin() = delegate.origin()

  def withFallback(other: ConfigMergeable) = delegate.withFallback(other)

  def resolve() = delegate.resolve()

  def resolve(options: ConfigResolveOptions) = delegate.resolve(options)

  def checkValid(reference: Config, restrictToPaths: String*) = delegate.checkValid(reference, restrictToPaths:_*)

  def hasPath(path: String) = delegate.hasPath(p(path))

  def isEmpty = delegate.isEmpty

  def entrySet(): util.Set[Entry[String, ConfigValue]] = delegate.entrySet()

  def getBoolean(path: String) = delegate.getBoolean(p(path))

  def getNumber(path: String) = delegate.getNumber(p(path))

  def getInt(path: String) = delegate.getInt(p(path))

  def getLong(path: String) = delegate.getLong(p(path))

  def getDouble(path: String) = delegate.getDouble(p(path))

  def getString(path: String) = delegate.getString(p(path))

  def getObject(path: String) = delegate.getObject(p(path))

  def getConfig(path: String) = new ConfigDelegate(delegate.getConfig(p(path)))

  def getAnyRef(path: String) = delegate.getAnyRef(p(path))

  def getValue(path: String) = delegate.getValue(p(path))

  def getBytes(path: String) = delegate.getBytes(p(path))

  def getMilliseconds(path: String) = delegate.getMilliseconds(p(path))

  def getNanoseconds(path: String) = delegate.getNanoseconds(p(path))

  def getList(path: String) = delegate.getList(p(path))

  def getBooleanList(path: String): util.List[Boolean] = delegate.getBooleanList(p(path))

  def getNumberList(path: String): util.List[Number] = delegate.getNumberList(p(path))

  def getIntList(path: String): util.List[Integer] = delegate.getIntList(p(path))

  def getLongList(path: String): util.List[Long] = delegate.getLongList(p(path))

  def getDoubleList(path: String): util.List[Double] = delegate.getDoubleList(p(path))

  def getStringList(path: String): util.List[String] = delegate.getStringList(p(path))

  def getObjectList(path: String): util.List[_ <: ConfigObject] = delegate.getObjectList(p(path))

  def getConfigList(path: String): util.List[_ <: Config] = delegate.getConfigList(p(path))

  def getAnyRefList(path: String) = delegate.getAnyRefList(p(path))

  def getBytesList(path: String): util.List[Long] = delegate.getBytesList(p(path))

  def getMillisecondsList(path: String): util.List[Long] = delegate.getMillisecondsList(p(path))

  def getNanosecondsList(path: String): util.List[Long] = delegate.getNanosecondsList(p(path))

  def withOnlyPath(path: String) = delegate.withOnlyPath(p(path))

  def withoutPath(path: String) = delegate.withoutPath(p(path))

  def atPath(path: String) = delegate.atPath(p(path))

  def atKey(key: String) = delegate.atKey(p(key))

  def withValue(path: String, value: ConfigValue) = delegate.withValue(p(path), value)
}

