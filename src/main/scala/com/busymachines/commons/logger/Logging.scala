package com.busymachines.commons.logger

import org.apache.logging.log4j.LogManager

trait Logging {
  type LogParameterAugmentor = Map[String, String] => Map[String, String]

  lazy val logger = LogManager.getLogger(getClass)

  implicit val defaultParameterAugmentor: LogParameterAugmentor = identity

  def trace(message: => String)(implicit parameterAugmentor: LogParameterAugmentor) {
    trace(message, Map.empty[String, String])(parameterAugmentor)
  }

  def trace(message: => String, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    trace(message, null, messageParameters)(parameterAugmentor)
  }

  def trace(message: => String, e: Throwable)(implicit parameterAugmentor: LogParameterAugmentor) {
    trace(message, e, Map.empty[String, String])(parameterAugmentor)
  }

  def trace(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    //TODO: call logger....
  }

  def info(message: => String)(implicit parameterAugmentor: LogParameterAugmentor) {
    info(message, Map.empty[String, String])(parameterAugmentor)
  }

  def info(message: => String, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    info(message, null, messageParameters)(parameterAugmentor)
  }

  def info(message: => String, e: Throwable)(implicit parameterAugmentor: LogParameterAugmentor) {
    info(message, e, Map.empty[String, String])(parameterAugmentor)
  }

  def info(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    //TODO: call logger....
  }

  def debug(message: => String)(implicit parameterAugmentor: LogParameterAugmentor) {
    debug(message, Map.empty[String, String])(parameterAugmentor)
  }

  def debug(message: => String, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    debug(message, null, messageParameters)(parameterAugmentor)
  }

  def debug(message: => String, e: Throwable)(implicit parameterAugmentor: LogParameterAugmentor) {
    debug(message, e, Map.empty[String, String])(parameterAugmentor)
  }

  def debug(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    //TODO: call logger....
  }

  def error(message: => String)(implicit parameterAugmentor: LogParameterAugmentor) {
    error(message, Map.empty[String, String])(parameterAugmentor)
  }

  def error(message: => String, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    error(message, null, messageParameters)(parameterAugmentor)
  }

  def error(message: => String, e: Throwable)(implicit parameterAugmentor: LogParameterAugmentor) {
    error(message, e, Map.empty[String, String])(parameterAugmentor)
  }

  def error(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    //TODO: call logger....
  }

  def fatal(message: => String)(implicit parameterAugmentor: LogParameterAugmentor) {
    fatal(message, Map.empty[String, String])(parameterAugmentor)
  }

  def fatal(message: => String, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    fatal(message, null, messageParameters)(parameterAugmentor)
  }

  def fatal(message: => String, e: Throwable)(implicit parameterAugmentor: LogParameterAugmentor) {
    fatal(message, e, Map.empty[String, String])(parameterAugmentor)
  }

  def fatal(message: => String, e: Throwable, messageParameters: => Map[String, String])(implicit parameterAugmentor: LogParameterAugmentor) {
    //TODO: call logger....
  }

}