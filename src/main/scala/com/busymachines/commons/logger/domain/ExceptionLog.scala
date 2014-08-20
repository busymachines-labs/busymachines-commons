package com.busymachines.commons.logger.domain

trait JsonFormat{
  def toJson:String
}
case class CodeLocationInfo(
  level: Option[String],
  className: Option[String],
  methodName: Option[String],
  fileName: Option[String],
  lineNumber: Option[Int],
  message: Option[String],
  time: Option[String],
  thread: Option[String]) extends JsonFormat{
  def toJson:String ={
???
  }
}

case class DefaultExceptionInfo(
  `type`: String = "StandardException",
  message: Option[String],
  cause: Option[String],
  stackTrace: List[String]) extends JsonFormat{
  def toJson:String = ???
}

case class CommonExceptionInfo(
  `type`: String = "CommonException",
  errorId: Option[String],
  message: Option[String],
  parameters: Option[String],
  cause: Option[String],
  stackTrace: List[String]) extends JsonFormat{
  def toJson:String = ???
}

case class LogMessage(
  codeLocationInfo: Option[CodeLocationInfo] = None,
  defaultExceptionInfo: Option[DefaultExceptionInfo] = None,
  commonExceptionInfo: Option[CommonExceptionInfo] = None) extends Serializable{
  def toJson= "LogMessage{"+codeLocationInfo.map(r=>r.toJson).getOrElse("")+defaultExceptionInfo.map(r=>r.toJson).getOrElse("")+commonExceptionInfo.map(r=>r.toJson).getOrElse("")+"}"

}