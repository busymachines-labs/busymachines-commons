package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import java.security.MessageDigest

class RichString(val s : String) extends AnyVal {
  def sha256Hash: Array[Byte] = 
    MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))

  def md5 : Array[Byte] = 
    MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"))
  
  def toOptionInt: Option[Int] = 
    try {
      Some(Integer.parseInt(s.trim))
    } catch {
      case e: NumberFormatException => None
    }
}