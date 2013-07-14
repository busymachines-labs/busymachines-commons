package com.busymachines.commons

import com.google.common.io.BaseEncoding

object RichByteArray {
  val base64Encoding = BaseEncoding.base64
}

class RichByteArray(val bytes : Array[Byte]) extends AnyVal {
  def encodeBase64 : String = 
    RichByteArray.base64Encoding.encode(bytes)
}