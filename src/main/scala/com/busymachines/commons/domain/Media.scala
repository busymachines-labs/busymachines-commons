package com.busymachines.commons.domain

case class Media(
  id : Id[Media]  = Id.generate, 
  mimeType : MimeType, 
  name : Option[String] = None, 
  data : Array[Byte]
) extends HasId[Media]

case class MimeType(value : String)

object MimeTypes {
  val gif  = MimeType("image/gif")
  val jpeg  = MimeType("image/jpeg")
  val pjpeg  = MimeType("image/pjpeg")
  val png  = MimeType("image/png")
  val svgXml  = MimeType("image/svg+xml")
  val tiff  = MimeType("image/tiff")
    
  val pdf  = MimeType("application/pdf")
  val xml  = MimeType("application/xml")
  val zip  = MimeType("application/zip")
  val gzip  = MimeType("application/gzip")
  val json  = MimeType("application/json")
  val javascript  = MimeType("application/javascript")
  val postscript  = MimeType("application/postscript")
    
  val css  = MimeType("text/css")
  val csv  = MimeType("text/csv")
  val html  = MimeType("text/html")
  val text  = MimeType("text/plain")

  def fromResourceName(name : String) : MimeType = {
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
      case i => MimeType("application/" + name.substring(i + 1))
    }
  }
}
