package com.busymachines.commons.implicits

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import java.security.MessageDigest
import java.lang.{ Long => JLong, Double => JDouble }
import com.busymachines.commons.spray.ProductFormat
import scala.reflect.ClassTag
import com.busymachines.commons.elasticsearch.ESField
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

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
    
  def copyTo(file: File, charset: Charset = StandardCharsets.UTF_8) {
    val writer = new OutputStreamWriter(new FileOutputStream(file), charset)
    try writer.write(s)
    finally writer.close()
  }

}