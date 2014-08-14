package com.busymachines.commons

@deprecated("use com.busymachines.commons.logger.Logging", "use com.busymachines.commons.logger.Logging")
trait Logging extends grizzled.slf4j.Logging {

  // Make logger implicit. This is useful for utility functions that take
  // a logger, like ProfilingUtils. Also strip the tailing $ from the logger
  // name.
  override implicit lazy val logger = {
    grizzled.slf4j.Logger(getClass.getName.stripSuffix("$"))
  }
}
