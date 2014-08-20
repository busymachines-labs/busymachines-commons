package com.busymachines.commons.logger.domain

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.commons.logger.domain.Implicits._

object LogMessageESMappings extends ESMapping[LogMessage] {
  val codeLocationInfo = "codeLocationInfo" :: Nested(CodeLocationInfoESMappings)
  val defaultExceptionInfo = "defaultExceptionInfo" :: Nested(DefaultExceptionInfoESMappings)
  val commonExceptionInfo = "commonExceptionInfo" :: Nested(CommonExceptionInfoESMappings)
  val time = "timestamp" :: Date
}

object CodeLocationInfoESMappings extends ESMapping[CodeLocationInfo] {
  val level = "level" :: String
  val className = "className" :: String
  val methodName = "methodName" :: String
  val fileName = "fileName" :: String
  val lineNumber = "lineNumber" :: Integer
  val message = "message" :: String
  val thread = "thread" :: String
}

object DefaultExceptionInfoESMappings extends ESMapping[DefaultExceptionInfo] {
  val `type` = "type" :: String
  val message = "message" :: String
  val cause = "cause" :: String
  val stackTrace = "stackTrace" :: String
}

object CommonExceptionInfoESMappings extends ESMapping[CommonExceptionInfo] {
  val `type` = "type" :: String
  val message = "message" :: String
  val cause = "cause" :: String
  val stackTrace = "stackTrace" :: String
  val errorId = "errorId" :: String
  val parameters = "parameters" :: String
}
