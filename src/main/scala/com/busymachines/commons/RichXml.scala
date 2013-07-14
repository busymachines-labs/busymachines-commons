package com.busymachines.commons

import java.net.URL
import scala.io.Source
import com.busymachines.commons.implicits._
import scala.xml.XML
import scala.xml.factory.XMLLoader
import scala.xml.Elem

class RichXml(val xml : XMLLoader[Elem]) extends AnyVal {

  def fromURL(username : String, password : String, url : String) : Elem =
    xml.load(new URL(url).openStream(username, password))
}