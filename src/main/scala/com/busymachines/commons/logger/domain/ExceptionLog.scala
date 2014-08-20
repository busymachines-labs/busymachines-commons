package com.busymachines.commons.logger.domain

import com.busymachines.commons.Implicits._

trait JsonFormat {
  def toJson: String = "{" + getAttr.filterNot(s => s == "").mkString(",") + "}"

  def getAttr: List[String]

  def formatValue[T <: JsonFormat](name: String, value: Option[T]): String = value match {
    case Some(v) =>
      s"""
         |"$name":${v.toJson}
       """.stripMargin
    case None => ""
  }

  //TYPE ERASURE :|
  def formatValue(name: String, value: Option[String], hack: String = "HAAACk"): String = value match {
    case Some(v) =>
      s"""
         |"$name":"$v"
       """.stripMargin
    case None => ""
  }

  def formatValue(name: String, value: List[String]): String = value match {
    case x :: xs =>
      s"""
         |"$name":"[${value.mkString(",")}]"
       """.stripMargin
    case _ => ""
  }
}

case class CodeLocationInfo( level: Option[String],
                             className: Option[String],
                             methodName: Option[String],
                             fileName: Option[String],
                             lineNumber: Option[Int],
                             message: Option[String],
                             thread: Option[String]) extends JsonFormat {
  def getAttr =List(formatValue("level", level),
    formatValue("className", className),
    formatValue("methodName", methodName),
    formatValue("fileName", fileName),
    formatValue("lineNumber", lineNumber.map(r => r.toString)),
    formatValue("message", message),
    formatValue("thread", thread)
  )
}

case class DefaultExceptionInfo(`type`: Option[String] = Some("StandardException"),
                                message: Option[String],
                                cause: Option[String],
                                stackTrace: List[String]) extends JsonFormat {
  def getAttr = List(formatValue("type", `type`), formatValue("@message", message), formatValue("cause", cause), formatValue("stackTrace", stackTrace))
}

case class CommonExceptionInfo(`type`: Option[String] = Some("CommonException"),
                               errorId: Option[String],
                               message: Option[String],
                               parameters: Option[String],
                               cause: Option[String],
                               stackTrace: List[String]) extends JsonFormat {
  def getAttr = List(formatValue("type", `type`),
    formatValue("errorId", errorId),
    formatValue("@message", message),
    formatValue("parameters", parameters),
    formatValue("cause", cause),
    formatValue("stackTrace", stackTrace))
}

case class LogMessage(codeLocationInfo: Option[CodeLocationInfo] = None,
                      defaultExceptionInfo: Option[DefaultExceptionInfo] = None,
                      commonExceptionInfo: Option[CommonExceptionInfo] = None,
                      time: Option[String]=None) extends Serializable with JsonFormat {
  def getAttr = List(formatValue("codeLocationInfo", codeLocationInfo),
    formatValue("defaultExceptionInfo", defaultExceptionInfo),
    formatValue("commonExceptionInfo", commonExceptionInfo),
    formatValue("@timestamp", time))
}