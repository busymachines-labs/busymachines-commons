package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import com.typesafe.config.impl.SimpleConfig
import com.typesafe.config.ConfigFactory
import scala.collection.mutable.ArrayBuffer

class RichConfig(val theConfig: Config) {

  def isDefined(path: String) = 
    theConfig.hasPath(path)
  
  def string(path: String) = 
    theConfig.getString(path)
    
  def stringOption(path: String): Option[String] =
    option(path, theConfig.getString).filterNot(_ == "")

  def stringSeq(path: String): Seq[String] =
    seq(path, theConfig.getStringList)

  def int(path: String) = 
    theConfig.getInt(path)
    
  def intOption(path: String): Option[Int] =
    option(path, theConfig.getInt)

  def intSeq(path: String): Seq[Int] =
    seq(path, theConfig.getIntList(_).map(_.toInt))

  def long(path: String) = 
    theConfig.getLong(path)
    
  def longOption(path: String): Option[Long] =
    option(path, theConfig.getLong)

  def longSeq(path: String): Seq[Long] =
    seq(path, theConfig.getLongList(_).map(_.toLong))

  def double(path: String) = 
    theConfig.getDouble(path)
    
  def doubleOption(path: String): Option[Double] =
    option(path, theConfig.getDouble)

  def doubleSeq(path: String): Seq[Double] =
    seq(path, theConfig.getDoubleList(_).map(_.toDouble))
    
  def boolean(path: String) = 
    theConfig.getBoolean(path)

  def booleanOption(path: String): Option[Boolean] =
    option(path, theConfig.getBoolean)
    
  def booleanSeq(path: String): Seq[Boolean] =
    seq(path, theConfig.getBooleanList(_).map(_.booleanValue))
    
  def bytes(path: String) = 
    theConfig.getBytes(path)

  def bytesOption(path: String): Option[Long] =
    option(path, theConfig.getBytes)

  def bytesSeq(path: String): Seq[Long] =
    seq(path, theConfig.getBytesList(_).map(_.longValue))

  def duration(path: String): FiniteDuration =
    theConfig.getMilliseconds(path).longValue.millis

  def durationOption(path: String): Option[FiniteDuration] =
    option(path, duration)

  def durationSeq(path: String): Seq[FiniteDuration] =
    seq(path, theConfig.getMillisecondsList(_).map(_.longValue.millis))
    
  def jodaDuration(path: String): org.joda.time.Duration = 
    new org.joda.time.Duration(theConfig.getMilliseconds(path))
    
  def jodaDurationOption(path: String): Option[org.joda.time.Duration] = 
    option(path, jodaDuration)
  
  def jodaDurationSeq(path: String): Seq[org.joda.time.Duration] = 
    seq(path, theConfig.getMillisecondsList(_).map(new org.joda.time.Duration(_)))
  
  def apply(path: String) = 
    config(path)
    
  def config(path: String) =
    if (path.isEmpty) this
    else new RichConfig(theConfig.getConfig(path))

  def configOption(path: String): Option[RichConfig] =
    option(path, config)

  def configOrEmpty(path: String): RichConfig =
    configOption(path).getOrElse(new RichConfig(ConfigFactory.empty))

  def configSeq(path: String) : Seq[RichConfig] =
    if (path.isEmpty) this :: Nil
    else theConfig.getConfigList(path).map(new RichConfig(_))

  def as[A](construct : Config => A) = 
    construct(theConfig)

  def asSeq[A](construct : Config => A) = {
    val result = ArrayBuffer[A]()
    var foundMore = true
    var index = 0
    while (foundMore) {
      if (theConfig.hasPath(index.toString)) {
        result += construct(theConfig.getConfig(index.toString))
        index += 1
      } else {
        foundMore = false
      }
    }
    result.toSeq
  }

  def mkString(sep: String) =
    toSortedStringSeq.mkString(sep)

  def toSortedStringSeq: Seq[String] =
    toSeq("", theConfig).sortWith(_ < _).map(x => x._1 + "=" + x._2)

  override def toString =
    toString("")
    
  def toString(prefixFilter : String) = 
    "\n  " + CommonConfig.toSortedStringSeq.filter(_.startsWith(prefixFilter)).mkString("\n  ")
    
  private def toSeq(prefix: String, config: Config): Seq[(String, String)] = {
    config.entrySet.toSeq.flatMap { entry =>
      entry.getValue match {
        case config: Config => toSeq(entry.getKey.replace("\"", "") + ".", config)
        case value => Seq((entry.getKey.replace("\"", ""), value.render))
      }
    }
  }
  
  private def option[A](path: String, f: String => A) =
    if (theConfig.hasPath(path)) Some(f(path))
    else None

  private def seq[A, B](path: String, f: String => java.util.List[A]) =
    if (theConfig.hasPath(path)) f(path).toSeq
    else Nil
}

