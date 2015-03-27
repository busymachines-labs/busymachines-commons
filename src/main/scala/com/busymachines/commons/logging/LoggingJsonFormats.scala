package com.busymachines.commons.logging

import com.busymachines.commons.Implicits._
import com.busymachines.commons.logging.domain._

object Implicits extends LoggingJsonFormats{}
trait LoggingJsonFormats {
  implicit val codeLocationInfoFormat = format4(CodeLocationInfo)
  implicit val defaultExceptionFormat = format4(DefaultExceptionInfo)

  implicit val harnessDataFormat = format2(HarnessData)
  implicit val commonExceptionFormat = format6(CommonExceptionInfo)
  implicit val logMessageFormat = format13(LogMessage)
}
