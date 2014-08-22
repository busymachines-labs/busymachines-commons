package com.busymachines.commons.logging

import org.apache.logging.log4j.message.MapMessage

/**
 *  This class is used to pass around the information from our Logger class to the actual log4j Logger.
 *
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 20.08.2014.
 */
class CommonsLoggerMessage private(
  val message: String,
  val cause: Option[Throwable],
  val parameters: Seq[(String, String)],
  javaMap: java.util.Map[String, String],
  val tag:Option[String]) extends MapMessage(javaMap) {

  override def getThrowable: Throwable = cause.orNull
}

object CommonsLoggerMessage {
  def apply(message: String, cause: Option[Throwable], parameters: Seq[(String, String)],tag: Option[String] = None) = {
    val javaMap = new java.util.HashMap[String, String]()
    javaMap.put("message", message)
    for ((key, value) <- parameters)
      javaMap.put(key, value)
    new CommonsLoggerMessage(message, cause, parameters, javaMap, tag)
  }
}