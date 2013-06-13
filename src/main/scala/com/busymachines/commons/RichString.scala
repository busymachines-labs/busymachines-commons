package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import java.security.MessageDigest

class RichString(val s : String) extends AnyVal {
  def sha256Hash: Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(s.getBytes("UTF-8"))
    digest.digest
  }
}