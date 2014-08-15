package com.busymachines.commons.logger.appender
import akka.actor.{Props, ActorSystem}
import com.busymachines.commons.CommonException
import com.busymachines.commons.logger.consumer.MessageActorConsumer
import com.busymachines.commons.logger.domain.CodeLocationInfo
import com.busymachines.commons.logger.domain.CommonExceptionInfo
import com.busymachines.commons.logger.domain.DefaultExceptionInfo
import com.busymachines.commons.logger.domain.LogMessage
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginElement, PluginFactory}
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.{Filter, Layout, LogEvent}
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Mateo on 15.08.2014.
 */

object ElasticActorAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
                     @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
                     @PluginAttribute("hostNames") hosts: String,
                     @PluginAttribute("port") port: String,
                     @PluginAttribute("clusterName") clusterName: String,
                     @PluginAttribute("indexNamePrefix") indexNamePrefix: String,
                     @PluginAttribute("indexNameDateFormat") indexNameDateFormat: String,
                     @PluginAttribute("indexDocumentType") indexDocumentType: String,
                     @PluginElement("Layout") layout: Layout[_ <: Serializable],
                     @PluginElement("Filters") filter: Filter): ElasticActorAppender = new ElasticActorAppender(name, layout, filter, ignoreExceptions, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType);
}


//TODO Check logger initializastion. Possible causes: actor system creation / sl4j bindings
@Plugin(name = "ElasticActor", category = "Core", elementType = "appender", printObject = true)
class ElasticActorAppender(name: String, layout: Layout[_ <: Serializable], filter: Filter, ignoreExceptions: Boolean, hosts: String, port: String, clusterName: String, indexNamePrefix: String, indexNameDateFormat: String, indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {

  val system = ActorSystem("Elastic")
  val elasticActor = system.actorOf(Props(new MessageActorConsumer(clusterName, hosts, port, indexNamePrefix, indexNameDateFormat,indexDocumentType)), name="Yo")


  override def append(event: LogEvent): Unit = {
    if (event.getSource().getClassName().contains("grizzled.slf4j.Logger"))
      return ;

    if(!(event.isInstanceOf[Log4jLogEvent]))
      return ;

    send(event)
  }

  def send(event: LogEvent) {
    val message: LogMessage = doLayout(event)
    try {
     elasticActor ! message
    }
    catch {
      case ex: Exception => println(ex)
    }
  }

  def doLayout(event: LogEvent): LogMessage = {
    val cli: CodeLocationInfo = createCodeLocation(event)
    val (exceptionFormat: Option[DefaultExceptionInfo], commonExceptionFormat: Option[CommonExceptionInfo]) = createExceptionInfo(event)

    LogMessage(cli, exceptionFormat, commonExceptionFormat)
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

  def createCodeLocation(event: LogEvent): CodeLocationInfo = {
    val cli: CodeLocationInfo = CodeLocationInfo(
      level = Some(event.getLevel().toString()),
      thread = Some(event.getThreadName()),
      className = Some(event.getSource().getClassName()),
      fileName = Some(event.getSource().getFileName()),
      methodName = Some(event.getSource().getMethodName()),
      lineNumber = Some(event.getSource().getLineNumber()),
      time = Some(DateTimeFormat.longDateTime().print(event.getTimeMillis())),
      message = Some(event.getMessage().getFormattedMessage()))
    cli
  }
}

