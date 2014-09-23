package com.busymachines.commons.logging

import java.util

import com.busymachines.commons.CommonException
import com.busymachines.commons.logging.domain.{CodeLocationInfo, CommonExceptionInfo, DefaultExceptionInfo, LogMessage}
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginFactory}
import org.apache.logging.log4j.core.layout.AbstractLayout
import org.apache.logging.log4j.message.MapMessage
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Matei on 15.08.2014.
 */

object ESLayout {
  @PluginFactory
  def createLayout(
                    @PluginAttribute("locationInfo") locationInfo: Boolean,
                    @PluginAttribute("properties") properties: Boolean,
                    @PluginAttribute("complete") complete: Boolean,
                    @PluginAttribute(value = "withCodeLocation", defaultBoolean = false) withCodeLoc: Boolean) = new ESLayout(locationInfo, properties, complete, withCodeLoc)

}

@Plugin(name = "ESLayout", category = "Core", elementType = "layout", printObject = true)
class ESLayout(locationInfo: Boolean, properties: Boolean, complete: Boolean, withCodeLoc: Boolean) extends AbstractLayout[LogMessage](null, null) {

  //TODO ???? Find a better way to serialize this
  override def toByteArray(event: LogEvent): Array[Byte] = return toSerializable(event).toString.getBytes

  override def getContentFormat: util.Map[String, String] = new java.util.HashMap[String, String]()

  override def getContentType: String = "text/plain"

  override def toSerializable(event: LogEvent): LogMessage = {

    val (exceptionFormat: Option[DefaultExceptionInfo], commonExceptionFormat: Option[CommonExceptionInfo]) = createExceptionInfo(event)

    LogMessage(codeLocationInfo = createCodeLocation(event),
      commonExceptionInfo = commonExceptionFormat,
      defaultExceptionInfo = exceptionFormat,
      timestamp = Some(new DateTime(event.getTimeMillis())),
      level = Some(event.getLevel().toString()),
      thread = Some(event.getThreadName()),
      message = Some(getLogMessage(event)),
      fields = getLogParams(event),
      tag = getLogTag(event)
    )

  }

  def getLogMessage(event:LogEvent) = event.getMessage match{
    case e:CommonsLoggerMessage=> e.message
    case _ => event.getMessage.getFormattedMessage
  }

  def getLogTag(event:LogEvent)= event.getMessage match{
    case e:CommonsLoggerMessage=> e.tag
    case _ => None
  }

  import scala.collection.JavaConversions._
  def getLogParams(event: LogEvent):Map[String,String] = event.getMessage match {
    case e: CommonsLoggerMessage => e.parameters.toMap[String,String]
    case e: MapMessage => {
      e.getData.toMap
    }
    case _ => Map.empty[String,String]
  }

  def createExceptionInfo(event: LogEvent): (Option[DefaultExceptionInfo], Option[CommonExceptionInfo]) = {
    val (exceptionFormat, commonExceptionFormat) = event.getThrown() match {
      case null => (None, None)
      case e: CommonException => {
        val cExInfo = CommonExceptionInfo(
          message = Some(e.getMessage),
          cause = Some(e.getCause.toString),
          stackTrace = e.getStackTrace().toList.map(_.toString),
          errorId = Some(e.errorId),
          parameters = Some(e.parameters.mkString(",")))
        (None, Some(cExInfo))
      }
      case e: Throwable => {
        val exInfo = DefaultExceptionInfo(
          message = Some(e.getMessage),
          cause = Option(e.getCause()).map(_.toString),
          stackTrace = e.getStackTrace().toList.map(_.toString))
        (Some(exInfo), None)
      }
    }
    (exceptionFormat, commonExceptionFormat)
  }

  def createCodeLocation(event: LogEvent): Option[CodeLocationInfo] = {
    withCodeLoc match {
      case true => Some(CodeLocationInfo(
        className = Some(event.getSource().getClassName()),
        fileName = Some(event.getSource().getFileName()),
        methodName = Some(event.getSource().getMethodName()),
        lineNumber = Some(event.getSource().getLineNumber())))
      case false => None
    }
  }
}
