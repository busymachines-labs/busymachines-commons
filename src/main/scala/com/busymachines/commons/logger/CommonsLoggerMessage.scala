package com.busymachines.commons.logger

import org.apache.logging.log4j.message.MapMessage
import java.util.{ Map, HashMap }

/**
 *  This class is used to pass around the information from our Logger class to the actual log4j Logger.
 *
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 20.08.2014.
 */
class CommonsLoggerMessage(
  val message: String,
  val cause: Option[Throwable],
  val parameters: Seq[(String, String)],
  javaMap: java.util.Map[String, String]) extends MapMessage(javaMap) {

  override def getThrowable(): Throwable = cause.orNull
}

object CommonsLoggerMessage {
  def apply(message: String, cause: Option[Throwable], parameters: Seq[(String, String)]) = {
    val paramsWithMessage = parameters :+ ("message", message)
    val javaMap: java.util.Map[String, String] = new HashMap[String, String]()
    paramsWithMessage.foreach(pair => javaMap.put(pair._1, pair._2))
    new CommonsLoggerMessage(message, cause, parameters, javaMap)
  }
}