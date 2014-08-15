package com.busymachines.commons.logger.appender

import java.util.concurrent.LinkedBlockingQueue

import com.busymachines.commons.CommonException
import com.busymachines.commons.logger.consumer.MessageQueueConsumer
import com.busymachines.commons.logger.domain._
import com.busymachines.commons.logger.layout.ElasticLayout
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginElement, PluginFactory}
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.{Filter, Layout, LogEvent}
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ElasticAsyncAppenderMQ {
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
    @PluginElement("Filters") filter: Filter): ElasticAsyncAppenderMQ = new ElasticAsyncAppenderMQ(name, layout, filter, ignoreExceptions, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType);
}


//TODO Check few document creation 5k+- vs 10K
@Plugin(name = "ElasticAsyncMQ", category = "Core", elementType = "appender", printObject = true)
class ElasticAsyncAppenderMQ(name: String, layout: Layout[_ <: Serializable], filter: Filter, ignoreExceptions: Boolean, hosts: String, port: String, clusterName: String, indexNamePrefix: String, indexNameDateFormat: String, indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {

  lazy val messageQueue = new LinkedBlockingQueue[LogMessage](10024)

  val consumer = initializeConsumer
  
  def initializeConsumer=new Thread(new MessageQueueConsumer(messageQueue, clusterName, hosts, port, indexNamePrefix, indexNameDateFormat, indexDocumentType)).start()

  override def append(event: LogEvent): Unit = {
    if (event.getSource().getClassName().contains("grizzled.slf4j.Logger"))
      return ;

    if(!(event.isInstanceOf[Log4jLogEvent]))
      return ;

      send(event)
  }

  def send(event: LogEvent)= getLayout match{
    case e:ElasticLayout=>
      try {
        messageQueue.put(e.toSerializable(event))
      }
      catch {
        case ex: Exception => println(s"Exception while appending to message queue ${ex.getMessage}")
      }
    case _ => println(s"Unsupported layout! $getLayout")
    }


}
