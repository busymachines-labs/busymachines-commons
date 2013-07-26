package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._

class RichConfig(config : Config) {

  def getStringOption(path : String) : Option[String] = {
    if (config.hasPath(path)) {
      config.getString(path) match {
        case value if value == "" => None
        case value => Some(value)
      }
    } else {
      None
    }
  }
  
  def getIntOption(path : String) : Option[Int] = 
    getStringOption(path).map(_.toInt)
  
  def getConfigOption(path : String) : Option[Config] =
    if (config.hasPath(path)) Some(config.getConfig(path))
    else None
    
  def getStringSeq(path : String) : Seq[String] = 
    if (config.hasPath(path)) config.getStringList(path).toSeq
    else Nil
  
  def mkString(sep : String) = 
    toSeq.mkString(sep)
  
  def toSeq : Seq[String] = 
    toSeq("", config).sortWith(_ < _).map(x => x._1 + "=" + x._2)
    
  private def toSeq(prefix : String, config : Config) : Seq[(String, String)] = {
    config.entrySet.toSeq.flatMap { entry => 
      entry.getValue match {
        case config : Config => toSeq(entry.getKey + ".", config)
        case value => Seq((entry.getKey, value.render))
      }
    }
  }
}