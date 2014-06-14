package com.busymachines.commons.implicits

import java.security.MessageDigest
import java.util.zip.CRC32

import com.busymachines.commons.util.HexString
import org.apache.commons.codec.binary.Base64

class RichByteArray(val bytes : Array[Byte]) extends AnyVal {
  def encodeBase64 : String =
    new String(Base64.encodeBase64(bytes), "UTF-8")

  def encodeBase64Url : String =
    new String(new Base64(true).encode(bytes), "UTF-8")

  def md5: Array[Byte] =
    MessageDigest.getInstance("MD5").digest(bytes)

  def crc32 = {
    val crc = new CRC32()
    crc.update(bytes)
    crc.getValue.toInt
  }

  def toHexString: String =
    HexString.fromByteArray(bytes)
    
  def toHexString(separator: String, groupSize: Int = 1): String = 
    HexString.fromByteArray(bytes, separator, groupSize)
}

