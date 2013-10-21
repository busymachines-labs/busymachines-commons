package com.busymachines.commons.domain

case class GeoPoint(
  lat: Double,
  lon: Double) {
  override def toString = 
    lat.toString + "," + lon.toString
}
  
object GeoPointParser {
  def parse(s : String) : GeoPoint = {
    s.indexOf(',') match {
      case -1 => throw new NumberFormatException(s"Not a valid lat/lon pair: $s")
      case i => GeoPoint(s.substring(0, i).toDouble, s.substring(i + 1).toDouble)
    }
  }
}
