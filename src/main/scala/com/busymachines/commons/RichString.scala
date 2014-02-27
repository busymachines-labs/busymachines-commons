package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import java.security.MessageDigest
import java.lang.{ Long => JLong, Double => JDouble }

class RichString(val s: String) extends AnyVal {

  def nonEmptyOrElse(other: String) =
    if (s.trim.nonEmpty) s
    else other

  def sha256Hash: Array[Byte] =
    MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))

  def md5: Array[Byte] =
    RichByteArray.md5.hashBytes(s.getBytes("UTF-8")).asBytes

  def crc32 =
    RichByteArray.crc32.hashBytes(s.getBytes("UTF-8")).asInt

  def decodeBase64: Array[Byte] =
    RichByteArray.base64Encoding.decode(s)

  def toIntOption: Option[Int] =
    try {
      Some(Integer.parseInt(s.trim))
    } catch {
      case e: NumberFormatException => None
    }

  def toIntOrElse(alt: Int) =
    toIntOption.getOrElse(alt)

  def toLongOption: Option[Long] =
    try {
      Some(JLong.parseLong(s.trim))
    } catch {
      case e: NumberFormatException => None
    }

  def toLongOrElse(alt: Long) =
    toLongOption.getOrElse(alt)

  def toDoubleOption: Option[Double] =
    try {
      Some(JDouble.parseDouble(s.trim))
    } catch {
      case e: NumberFormatException => None
    }

  def toDoubleOrElse(alt: Double) =
    toDoubleOption.getOrElse(alt)
}