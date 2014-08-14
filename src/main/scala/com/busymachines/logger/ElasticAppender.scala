package com.busymachines.logger

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.transport._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.busymachines.logger.domain.CodeLocationInfo
import com.busymachines.logger.domain.LogMessage
import spray.json.pimpAny
import com.busymachines.logger.domain.Implicits._
import com.busymachines.commons.CommonException
import com.busymachines.logger.domain.CommonExceptionInfo
import com.busymachines.logger.domain.DefaultExceptionInfo

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ElasticAppender {
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
    @PluginElement("Filters") filter: Filter): ElasticAppender = new ElasticAppender(name, layout, filter, ignoreExceptions, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType);
}

@Plugin(name = "Elastic", category = "Core", elementType = "appender", printObject = true)
class ElasticAppender(name: String, layout: Layout[_ <: Serializable], filter: Filter, ignoreExceptions: Boolean, hosts: String, port: String, clusterName: String, indexNamePrefix: String, indexNameDateFormat: String, indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {
  lazy val logger = LogManager.getLogger()
  lazy val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", clusterName))
    .addTransportAddresses(hosts.split(",").map(new InetSocketTransportAddress(_, port.toInt)): _*)

  lazy val actualIndexName = s"$indexNamePrefix-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  logger.debug(s"Config : host=$hosts port=$port clusterName=$clusterName indexNamePrefix=$indexNamePrefix indexNameDateFormat=$indexNameDateFormat indexDocumentType=$indexDocumentType")
  override def append(event: LogEvent): Unit = {
    send(event)
  }

  def send(event: LogEvent) {
    val cli: CodeLocationInfo = CodeLocationInfo(
      level = Some(event.getLevel().toString()),
      thread = Some(event.getThreadName()),
      className = Some(event.getSource().getClassName()),
      fileName = Some(event.getSource().getFileName()),
      methodName = Some(event.getSource().getMethodName()),
      lineNumber = Some(event.getSource().getLineNumber()),
      time = Some(DateTimeFormat.longDateTime().print(event.getTimeMillis())),
      message = Some(event.getMessage().getFormattedMessage()))

    val (exceptionFormat, commonExceptionFormat) = event.getThrown() match {
      case null => (None, None)
      case e: CommonException => {
        val cExInfo = CommonExceptionInfo(
          message = Some(e.getMessage),
          cause = Some(e.getCause.toString),
          stackTrace = e.getStackTrace().toList.map(_.toString),
          errorId = Some(e.errorId),
          parameters = e.parameters)
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
    val message: LogMessage = LogMessage(cli, exceptionFormat, commonExceptionFormat)
    val messageJson = message.toJson.prettyPrint.toString
    try {
      client.prepareIndex(
        actualIndexName, indexDocumentType)
        .setSource(messageJson)
        .execute
        .actionGet
    } catch {
      case ex: Exception =>
        println(s"Exception while using ElasticSearch client! ${ex.getMessage()}")
    } finally {
    }
  }
}
