package com.busymachines.commons.domain

case class Media(
  id : Id[Media] = Id.generate, 
  mimeType : String, 
  name : Option[String] = None, 
  data : Array[Byte]
) extends HasId[Media]

object MimeType {

  val gif = "image/gif"
  val jpeg = "image/jpeg"
  val pjpeg = "image/pjpeg"
  val png = "image/png"
  val svgXml = "image/svg+xml"
  val tiff = "image/tiff"
    
  val pdf = "application/pdf"
  val xml = "application/xml"
  val zip = "application/zip"
  val gzip = "application/gzip"
  val json = "application/json"
  val javascript = "application/javascript"
  val postscript = "application/postscript"
    
  val css = "text/css"
  val csv = "text/csv"
  val html = "text/html"
  val text = "text/plain"

  def fromResourceName(name : String) : String = {
    if (name.endsWith(".gif")) gif
    if (name.endsWith(".jpeg")) jpeg
    if (name.endsWith(".pjpeg")) pjpeg
    if (name.endsWith(".png")) png
    if (name.endsWith(".svgXml")) svgXml
    if (name.endsWith(".pdf")) pdf
    if (name.endsWith(".xml")) xml
    if (name.endsWith(".zip")) zip
    if (name.endsWith(".gzip")) gzip
    if (name.endsWith(".json")) json
    if (name.endsWith(".js")) javascript
    if (name.endsWith(".ps")) postscript
    if (name.endsWith(".css")) css
    if (name.endsWith(".csv")) csv
    if (name.endsWith(".html")) html
    if (name.endsWith(".text")) text
    else name.lastIndexOf('.') match {
      case -1 => text
      case i => "application/" + name.substring(i + 1)
    }
  }
}
