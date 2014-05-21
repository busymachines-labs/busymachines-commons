package com.busymachines.commons.util

import scala.collection.mutable.ArrayBuffer

object HexString {
  
  private val hexDigits = "0123456789ABCDEF".toCharArray()

  def fromByteArray(bytes: Array[Byte]): String =
    fromByteArray(bytes, None, 0)
    
  def fromByteArray(bytes: Array[Byte], separator: String, groupSize: Int = 1): String = 
    fromByteArray(bytes, Some(separator), groupSize)

  def toByteArray(s: String): Array[Byte] = { 
    var last = -1
    def group(i: Int) =
     if (last < 0) { last = i; None}
     else { val r = last * 16 + i; last = -1; Some(r)}
    def filter(c: Char) = 
      if (c >= '0' && c <= '9') Some(c - '0')
      else if (c >= 'A' && c <= 'F') Some(c - 'A' + 10)
      else if (c >= 'a' && c <= 'f') Some(c - 'a' + 10)
      else None
    for (c: Char <- s.toArray; i <- filter(c); i2 <- group(i)) yield i2.toByte
  }
    
  private def fromByteArray(bytes: Array[Byte], separator: Option[String], groupSize: Int): String = {
    val sb = new StringBuilder(2 * bytes.size)
    var pos = 0
    for (b <- bytes) {
      if (separator.isDefined && pos == groupSize) {
        pos = 0
        sb.append(separator.get)
      }
      pos += 1
      sb.append(hexDigits((b >> 4) & 0xf)).append(hexDigits(b & 0xf))
    }
    sb.toString()
  } 
}
