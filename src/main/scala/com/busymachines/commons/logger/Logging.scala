package com.busymachines.commons.logger

import org.apache.logging.log4j.{Level, LogManager}

trait AdditionalParameters {
  def apply(originalParameters: Map[String, String]): Map[String, String]
}

object DefaultAdditionalParameters extends AdditionalParameters {
  def apply(originalParameters: Map[String, String]) = originalParameters
}

trait Logging {
  implicit def defaultAdditionalParameters = DefaultAdditionalParameters
  val logger = new Logger()
}

sealed class Logger {
  private lazy val logger = LogManager.getLogger

  def isTraceEnabled = logger.isTraceEnabled
  def isDebugEnabled = logger.isDebugEnabled
  def isInfoEnabled = logger.isInfoEnabled
  def isWarnEnabled = logger.isWarnEnabled
  def isErrorEnabled = logger.isErrorEnabled
  def isFatalEnabled = logger.isFatalEnabled

  def trace(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.TRACE, message, None, parameters)

  def trace(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.TRACE, message, Some(e), parameters)

  def debug(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.DEBUG, message, None, parameters)

  def debug(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.DEBUG, message, Some(e), parameters)

  def info(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.INFO, message, None, parameters)

  def info(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.INFO, message, Some(e), parameters)

  def warn(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.WARN, message, None, parameters)

  def warn(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.WARN, message, Some(e), parameters)

  def error(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.ERROR, message, None, parameters)

  def error(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.ERROR, message, Some(e), parameters)

  def fatal(message: => String, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.FATAL, message, None, parameters)

  def fatal(message: => String, e: => Throwable, parameters: (String, String)*)(implicit ap: AdditionalParameters) =
    log(Level.FATAL, message, Some(e), parameters)

  private def log(level: Level, message: => String, cause: Option[Throwable], parameters: Seq[(String, String)])(implicit ap: AdditionalParameters) {
    if (logger.isEnabled(level)) {
      val commonsLogMessage = CommonsLoggerMessage(message, cause.orNull, ap(parameters.toMap))
      logger.log(level, commonsLogMessage, cause.orNull)
    }
  }
}