package com.busymachines.commons

import java.net.URL
import scala.io.Source
import com.busymachines.commons.implicits._

object RichSource {

  def fromURL(username : String, password : String, url : String) = 
    Source.fromInputStream(new URL(url).openStream(username, password))
}