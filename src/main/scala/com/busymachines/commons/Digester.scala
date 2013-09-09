package com.busymachines.commons

import com.google.common.hash.Hashing

@deprecated // please use RichByteArray.md5 etc
class Digester {
  
  private val hashing = Hashing.md5()

  def digest(plainText: String) = hashing.hashString(plainText)
  def digest(plainText: Array[Byte]) = hashing.hashBytes(plainText)
  def matches(plainText: String, digestedText: String): Boolean = 
    digest(plainText).toString.equals(digestedText)

}