package com.busymachines.commons.logger

import java.util.concurrent.LinkedBlockingQueue

import com.busymachines.commons.elasticsearch.{ESCollection, ESConfig, ESIndex}
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.logger.domain.{LogMessage, LoggerESTypes}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginElement, PluginFactory}
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.{Filter, Layout, LogEvent}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ESAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
                     @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
                     @PluginAttribute("queueSize") queueSize: Int,
                     @PluginAttribute("bulkSize") bulkSize: Int,
                     @PluginAttribute(value ="hostNames", defaultString = "localhost") hosts: String,
                     @PluginAttribute(value ="port", defaultInt = 9300) port: Int,
                     @PluginAttribute(value ="clusterName", defaultString = "elasticsearch") clusterName: String,
                     @PluginAttribute(value ="indexNamePrefix", defaultString = "logstash") indexNamePrefix: String,
                     @PluginAttribute(value ="indexNameDateFormat", defaultString = "yyyy.MM.dd") indexNameDateFormat: String,
                     @PluginAttribute(value ="indexDocumentType", defaultString = "log") indexDocumentType: String,
                     @PluginElement("Layout") layout: Layout[_ <: Serializable],
                     @PluginElement("Filters") filter: Filter): ESAppender = new ESAppender(name, layout, filter, ignoreExceptions, queueSize, bulkSize, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType)
}

@Plugin(name = "ESAppender", category = "Core", elementType = "appender", printObject = true)
class ESAppender(
                  name: String,
                  layout: Layout[_ <: Serializable],
                  filter: Filter,
                  ignoreExceptions: Boolean,
                  queueSize: Int,
                  bulkSize: Int,
                  hosts: String,
                  portNo: Int,
                  cluster: String ,
                  indexNamePrefix: String ,
                  indexNameDateFormat: String,
                  indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {

  lazy val messageQueue = new LinkedBlockingQueue[LogMessage](queueSize)
  lazy val actualIndexName = s"$indexNamePrefix.${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"
  lazy val collection = {
    val index = new ESIndex(
      new ESConfig(""){
        override def clusterName: String = cluster
        override def indexName: String = actualIndexName
        override def hostNames: Seq[String] = hosts.split(",")
        override def numberOfShards: Int = 1
        override def numberOfReplicas: Int = 0
        override def port: Int = portNo
      }, DoNothingEventSystem)

    val collection = new ESCollection[LogMessage](index, LoggerESTypes.LogMessage)
    collection
  }

  val process = Future {
    while (true) {
      if (messageQueue.size() >= bulkSize)
        try {
          val listBuffer: ListBuffer[LogMessage] = ListBuffer[LogMessage]()
          (0 until bulkSize).foreach(i => {
            listBuffer += messageQueue.take()})
          collection.bulk(listBuffer)
        } catch {
          case ex: Exception => println(ex)
        }
    }
  }

  override def append(event: LogEvent): Unit = {
    if (event.getSource().getClassName().contains("grizzled.slf4j.Logger"))
      return;

    if (!(event.isInstanceOf[Log4jLogEvent]))
      return;

    send(event)
  }

  def send(event: LogEvent) {
    getLayout match {
      case e: ESLayout =>
        try {
          messageQueue.put(e.toSerializable(event))
        }
        catch {
          case ex: Exception => println(s"Exception while appending to message queue ${ex.getMessage}")
        }
      case _ => println(s"Unsupported layout! $getLayout")
    }
  }
}



