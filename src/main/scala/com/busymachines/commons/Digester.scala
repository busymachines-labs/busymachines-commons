package com.busymachines.commons

import com.google.common.hash.Hashing

class Digester {
  
  private val hashing = Hashing.md5()

  def digest(plainText: String) = hashing.hashString(plainText)
  def matches(plainText: String, digestedText: String): Boolean =
    digest(plainText).toString.equals(digestedText)

}