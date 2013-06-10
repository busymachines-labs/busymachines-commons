package com.busymachines.commons

trait Logging extends grizzled.slf4j.Logging {

  // Make logger implicit. This is useful for utility functions that take
  // a logger, like ProfilingUtils. Also strip the trailing $ from the logger
  // name.
  override implicit lazy val logger = {
    grizzled.slf4j.Logger(getClass.getName.stripSuffix("$"))
  }
}
