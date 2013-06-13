package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._

class RichConfig(config : Config) {

  def getOptionalInt(path : String) : Option[Int] = {
    config.getString(path) match {
      case value if value == "" => None
      case value => Some(value.toInt)
    }
  }
  
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