package com.busymachines.commons.logger

import org.apache.logging.log4j.message.StructuredDataMessage

/**
 *  This class is used to pass around the information from our Logger class to the actual log4j Logger.
 *
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 20.08.2014.
 */
class CommonsLoggerMessage(
  val message: String,
  val cause: Option[Throwable],
  val messageParameters: Map[String, String])
  extends org.apache.logging.log4j.message.Message {
  override def getFormattedMessage(): String = message
  override def getFormat(): String = ""
  override def getParameters(): Array[Object] = null
  override def getThrowable(): Throwable = cause.orNull
}

object CommonsLoggerMessage {
  def apply(message: String, cause: Throwable, messageParameters: Map[String, String]) = 
    new CommonsLoggerMessage(message, if (cause == null) None else Some(cause), messageParameters)
}