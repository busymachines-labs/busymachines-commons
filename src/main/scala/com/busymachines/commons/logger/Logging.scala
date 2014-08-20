package com.busymachines.commons.logger

import org.apache.logging.log4j.{ Level, LogManager }

trait AdditionalParameters {
  def apply(originalMap: Map[String, String]): Map[String, String]
}

trait Logging {
  implicit val defaultAdditionalParameters: AdditionalParameters = new AdditionalParameters { def apply(originalMap: Map[String, String]): Map[String, String] = originalMap }
  val logger = new Logger()
}

sealed class Logger {
  private lazy val logger = LogManager.getLogger()

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
    if (logger.isEnabled(Level.TRACE)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.trace(commonsLogMessage, e)
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
    if (logger.isEnabled(Level.INFO)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.info(commonsLogMessage, e)
    }
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
    if (logger.isEnabled(Level.WARN)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.warn(commonsLogMessage, e)
    }
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
    if (logger.isEnabled(Level.DEBUG)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.debug(commonsLogMessage, e)
    }
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
    if (logger.isEnabled(Level.ERROR)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.error(commonsLogMessage, e)
    }
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
    if (logger.isEnabled(Level.FATAL)) {
      val commonsLogMessage = CommonsLoggerMessage(message, e, additionalParameters(messageParameters))
      logger.fatal(commonsLogMessage, e)
    }
  }

}