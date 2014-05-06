package com.busymachines.commons

import java.io.InputStream
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.File

class RichInputStream(is: InputStream) {

  /**
   * Copies the contents of this input stream to given output stream.
   * The input stream will be closed, the output stream is NOT.
   */
  def copyTo(os: OutputStream) {
    val buf = new Array[Byte](100)
    try {
      var len = 0
      while (len != -1) {
        if (len > 0)
          os.write(buf, 0, len)
        len = is.read(buf)
      }
    }
    finally {
      is.close()      
    }
  }
  
  def copyTo(file: File) {
    val os = new FileOutputStream(file)
    try copyTo(os)
    finally os.close()
  }
  
  def checkNotNull(msg: => String) = {
    if (is == null)
      throw new Exception(msg)
    is
  }
}