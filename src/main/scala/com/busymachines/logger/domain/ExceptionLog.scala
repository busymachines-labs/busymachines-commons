package com.busymachines.logger.domain

case class CodeLocationInfo(
  level: Option[String],
  className: Option[String],
  methodName: Option[String],
  fileName: Option[String],
  lineNumber: Option[Int],
  message: Option[String],
  time: Option[String],
  thread: Option[String])

case class DefaultExceptionInfo(
  `type`: String = "StandardException",
  message: Option[String],
  cause: Option[String],
  stackTrace: List[String])

case class CommonExceptionInfo(
  `type`: String = "CommonException",
  errorId: Option[String],
  message: Option[String],
  parameters: Map[String, String],
  cause: Option[String],
  stackTrace: List[String])

case class LogMessage(
  codeLocation: CodeLocationInfo,
  defaultException: Option[DefaultExceptionInfo] = None,
  commonException: Option[CommonExceptionInfo] = None)