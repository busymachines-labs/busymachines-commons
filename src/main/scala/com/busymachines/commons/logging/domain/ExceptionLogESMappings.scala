package com.busymachines.commons.logging.domain

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.commons.logging.Implicits

import Implicits._
import com.busymachines.commons.Implicits._

object LogMessageESMappings extends ESMapping[LogMessage] {
  val level = "level" :: String
  val time = "timestamp" :: Date
  val message = "message" :: String
  val tag = "tag" :: String
  val thread = "thread" :: String
  val fields = "fields" :: Object[Map[String,String]]
  val codeLocationInfo = "codeLocationInfo" :: Nested(CodeLocationInfoESMappings)
  val defaultExceptionInfo = "defaultExceptionInfo" :: Nested(DefaultExceptionInfoESMappings)
  val commonExceptionInfo = "commonExceptionInfo" :: Nested(CommonExceptionInfoESMappings)
}

object CodeLocationInfoESMappings extends ESMapping[CodeLocationInfo] {
  val className = "className" :: String
  val methodName = "methodName" :: String
  val fileName = "fileName" :: String
  val lineNumber = "lineNumber" :: Integer
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
