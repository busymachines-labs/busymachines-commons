package com.busymachines.commons

import java.net.URL
import java.io.InputStream
import com.busymachines.commons.implicits._
  
class RichUrl(val url : URL) extends AnyVal {
  def openStream(username : String, password : String) : InputStream = {
    val uc = url.openConnection()
    val userpass = username + ":" + password
    val basicAuth = "Basic " + userpass.getBytes("ISO-8859-1").encodeBase64
    uc.setRequestProperty ("Authorization", basicAuth);
    uc.getInputStream();
  }
}