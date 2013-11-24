package com.busymachines.commons

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.classic.Level
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

trait Logging extends grizzled.slf4j.Logging {

  // Make logger implicit. This is useful for utility functions that take
  // a logger, like ProfilingUtils. Also strip the tailing $ from the logger
  // name.
  override implicit lazy val logger = {
    grizzled.slf4j.Logger(getClass.getName.stripSuffix("$"))
  }
}
