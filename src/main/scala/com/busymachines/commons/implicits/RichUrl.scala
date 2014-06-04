package com.busymachines.commons.implicits

import java.net.URL
import java.io.InputStream
import com.busymachines.commons.Implicits._
import java.io.File
  
class RichUrl(val url : URL) extends AnyVal {

  def fileName = {
    val path = url.getPath
    val index = path.lastIndexOf('/')
    if (index >= 0) path.substring(index + 1)
    else path
      
  } 
  
  /**
   * Opens a URL using basic authentication
   */
  def openStream(username : String, password : String) : InputStream = {
    val uc = url.openConnection()
    val userpass = username + ":" + password
    val basicAuth = "Basic " + userpass.getBytes("ISO-8859-1").encodeBase64
    uc.setRequestProperty ("Authorization", basicAuth)
    uc.getInputStream.checkNotNull(s"URL not accessible: $url")
  }
  
  def copyTo(file: File) {
    url.openStream.checkNotNull(s"URL not accessible: $url").copyTo(file)
  }
}