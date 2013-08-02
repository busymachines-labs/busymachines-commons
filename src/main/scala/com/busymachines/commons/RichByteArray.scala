package com.busymachines.commons

import com.google.common.io.BaseEncoding
import com.google.common.hash.Hashing

object RichByteArray {
  val base64Encoding = BaseEncoding.base64
  val crc32 = Hashing.crc32
  val md5 = Hashing.md5
}

class RichByteArray(val bytes : Array[Byte]) extends AnyVal {
  def encodeBase64 : String = 
    RichByteArray.base64Encoding.encode(bytes)

  def md5 = 
    RichByteArray.md5.hashBytes(bytes).asBytes

  def crc32 = 
    RichByteArray.crc32.hashBytes(bytes).padToLong
    
}