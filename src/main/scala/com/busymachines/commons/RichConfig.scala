package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import com.typesafe.config.impl.SimpleConfig
import com.typesafe.config.ConfigFactory

class RichConfig(config: Config) {

  private def option[A](path: String, f: String => A) =
    if (config.hasPath(path)) Some(f(path))
    else None

  private def seq[A, B](path: String, f: String => java.util.List[A]) =
    if (config.hasPath(path)) f(path).toSeq
    else Nil

  def getStringOption(path: String): Option[String] =
    option(path, config.getString).filterNot(_ == "")

  def getStringSeq(path: String): Seq[String] =
    seq(path, config.getStringList)

  def getIntOption(path: String): Option[Int] =
    option(path, config.getInt)

  def getIntSeq(path: String): Seq[Int] =
    seq(path, config.getIntList(_).map(_.toInt))

  def getLongOption(path: String): Option[Long] =
    option(path, config.getLong)

  def getLongSeq(path: String): Seq[Long] =
    seq(path, config.getLongList(_).map(_.toLong))

  def getDoubleOption(path: String): Option[Double] =
    option(path, config.getDouble)

  def getDoubleSeq(path: String): Seq[Double] =
    seq(path, config.getDoubleList(_).map(_.toDouble))
    
  def getBooleanOption(path: String): Option[Boolean] =
    option(path, config.getBoolean)

  def getBooleanSeq(path: String): Seq[Boolean] =
    seq(path, config.getBooleanList(_).map(_.booleanValue))
    
  def getBytesOption(path: String): Option[Long] =
    option(path, config.getBytes)

  def getBytesSeq(path: String): Seq[Long] =
    seq(path, config.getBytesList(_).map(_.longValue))

  def getDuration(path: String): Duration =
    config.getMilliseconds(path).longValue.millis

  def getDurationOption(path: String): Option[Duration] =
    if (config.hasPath(path)) Some(getDuration(path))
    else None

  def getDurationSeq(path: String): Seq[Duration] =
    if (config.hasPath(path)) config.getMillisecondsList(path).map(_.longValue.millis).toSeq
    else Nil

  def getConfigOption(path: String): Option[Config] =
    if (config.hasPath(path)) Some(config.getConfig(path))
    else None

  def getConfigOrEmpty(path: String): Config =
    if (config.hasPath(path)) config.getConfig(path)
    else ConfigFactory.empty

  def mkString(sep: String) =
    toSeq.mkString(sep)

  def toSeq: Seq[String] =
    toSeq("", config).sortWith(_ < _).map(x => x._1 + "=" + x._2)

  private def toSeq(prefix: String, config: Config): Seq[(String, String)] = {
    config.entrySet.toSeq.flatMap { entry =>
      entry.getValue match {
        case config: Config => toSeq(entry.getKey + ".", config)
        case value => Seq((entry.getKey, value.render))
      }
    }
  }
}