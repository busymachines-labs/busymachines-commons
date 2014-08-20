package com.busymachines.commons.logger

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message.StructuredDataMessage
import org.apache.logging.log4j.message.MapMessage

trait AdditionalParameters {
  def apply(originalMap: Map[String, String]): Map[String, String]
}

trait Logging {
  implicit val defaultAdditionalParameters: AdditionalParameters = new AdditionalParameters { def apply(originalMap: Map[String, String]): Map[String, String] = originalMap }
  //TODO: instance of Logger
  val logger = new Logger()
}

class Logger {
  private lazy val logger = LogManager.getLogger(getClass)

  private def toJavaMap(scalaMap: Map[String, String]): java.util.Map[String, String] = {
    null
  }

  def isDebugEnabled(): Boolean = logger.isDebugEnabled()

  def trace(message: => String)(implicit additionalParameters: AdditionalParameters) {
    trace(message, Map.empty[String, String])(additionalParameters)
  }

  def trace(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    trace(message, null, messageParameters)(additionalParameters)
  }

  def trace(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    trace(message, e, Map.empty[String, String])(additionalParameters)
  }

  def trace(message: => String, e: => Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    if (logger.isEnabled(Level.ALL)) {
      val structuredMessage = new MapMessage(toJavaMap(additionalParameters(messageParameters)))
      logger.trace(message, e)
    }
  }

  def info(message: => String)(implicit additionalParameters: AdditionalParameters) {
    info(message, Map.empty[String, String])(additionalParameters)
  }

  def info(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    info(message, null, messageParameters)(additionalParameters)
  }

  def info(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    info(message, e, Map.empty[String, String])(additionalParameters)
  }

  def info(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    //TODO: call logger....
  }

  def warn(message: => String)(implicit additionalParameters: AdditionalParameters) {
    warn(message, Map.empty[String, String])(additionalParameters)
  }

  def warn(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    warn(message, null, messageParameters)(additionalParameters)
  }

  def warn(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    warn(message, e, Map.empty[String, String])(additionalParameters)
  }

  def warn(message: => String, e: => Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    //TODO: add call to logger
  }

  def debug(message: => String)(implicit additionalParameters: AdditionalParameters) {
    debug(message, Map.empty[String, String])(additionalParameters)
  }

  def debug(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    debug(message, null, messageParameters)(additionalParameters)
  }

  def debug(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    debug(message, e, Map.empty[String, String])(additionalParameters)
  }

  def debug(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    //TODO: call logger....
  }

  def error(message: => String)(implicit additionalParameters: AdditionalParameters) {
    error(message, Map.empty[String, String])(additionalParameters)
  }

  def error(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    error(message, null, messageParameters)(additionalParameters)
  }

  def error(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    error(message, e, Map.empty[String, String])(additionalParameters)
  }

  def error(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    //TODO: call logger....
  }

  def fatal(message: => String)(implicit additionalParameters: AdditionalParameters) {
    fatal(message, Map.empty[String, String])(additionalParameters)
  }

  def fatal(message: => String, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    fatal(message, null, messageParameters)(additionalParameters)
  }

  def fatal(message: => String, e: Throwable)(implicit additionalParameters: AdditionalParameters) {
    fatal(message, e, Map.empty[String, String])(additionalParameters)
  }

  def fatal(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit additionalParameters: AdditionalParameters) {
    //TODO: call logger....
  }

}