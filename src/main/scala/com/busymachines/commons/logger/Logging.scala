package com.busymachines.commons.logger

import org.apache.logging.log4j.LogManager

trait Logging {
  lazy val logger = LogManager.getLogger(getClass)
}