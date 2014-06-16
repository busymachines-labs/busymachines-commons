package com.busymachines.commons.implicits

import java.io.{File, FileOutputStream, OutputStreamWriter}
import java.lang.{Double => JDouble, Long => JLong}
import java.nio.charset.{Charset, StandardCharsets}
import java.security.MessageDigest

import com.busymachines.commons.Implicits._
import org.apache.commons.codec.binary.Base64

class RichString(val s: String) extends AnyVal {

  def nonEmptyOrElse(other: String) =
    if (s.trim.nonEmpty) s
    else other

  def sha256Hash: Array[Byte] =
    MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))

  def md5: Array[Byte] =
    s.getBytes("UTF-8").md5

  def crc32: Int =
    s.getBytes("UTF-8").crc32

  def decodeBase64: Array[Byte] =
    Base64.decodeBase64(s)

  def decodeBase64Url: Array[Byte] =
    new Base64(true).decode(s)

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
    try {
      val writer = new OutputStreamWriter(new FileOutputStream(file), charset)
      try writer.write(s)
      finally writer.close()
    } catch {
      case e: Exception => throw new Exception("couldn't write to " + file + ": " + e.getMessage)
    }
  }

}