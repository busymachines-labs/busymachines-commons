package com.busymachines.commons.logging

import com.busymachines.commons.Implicits._
import com.busymachines.commons.logging.domain.{CodeLocationInfo, CommonExceptionInfo, DefaultExceptionInfo, LogMessage}

object Implicits extends LoggingJsonFormats{}
trait LoggingJsonFormats {
  implicit val codeLocationInfoFormat = format4(CodeLocationInfo)
  implicit val defaultExceptionFormat = format4(DefaultExceptionInfo)

  implicit val commonExceptionFormat = format6(CommonExceptionInfo)
  implicit val logMessageFormat = format8(LogMessage)
}
