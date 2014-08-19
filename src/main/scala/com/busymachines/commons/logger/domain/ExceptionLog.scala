package com.busymachines.commons.logger.domain

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
  parameters: Option[String],
  cause: Option[String],
  stackTrace: List[String])

case class LogMessage(
  codeLocationInfo: CodeLocationInfo,
  defaultExceptionInfo: Option[DefaultExceptionInfo] = None,
  commonExceptionInfo: Option[CommonExceptionInfo] = None) extends Serializable{
  def toJson= ???
}