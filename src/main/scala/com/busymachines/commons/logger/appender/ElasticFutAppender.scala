package com.busymachines.commons.logger.appender

import java.util.concurrent.LinkedBlockingQueue

import com.busymachines.commons.elasticsearch.{ESConfig, ESCollection, ESIndex}
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.logger.domain.{LoggerESTypes, LogMessage}
import com.busymachines.commons.logger.layout.ElasticLayout
import com.busymachines.commons.testing.DefaultTestESConfig
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.{LogEvent, Filter, Layout}
import org.apache.logging.log4j.core.config.plugins.{PluginElement, Plugin, PluginAttribute, PluginFactory}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ElasticFutAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
                     @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
                     @PluginAttribute("queueSize") queueSize: Int,
                     @PluginAttribute("bulkSize") bulkSize: Int,
                     @PluginAttribute("hostNames") hosts: String,
                     @PluginAttribute("port") port: String,
                     @PluginAttribute("clusterName") clusterName: String,
                     @PluginAttribute("indexNamePrefix") indexNamePrefix: String,
                     @PluginAttribute("indexNameDateFormat") indexNameDateFormat: String,
                     @PluginAttribute("indexDocumentType") indexDocumentType: String,
                     @PluginElement("Layout") layout: Layout[_ <: Serializable],
                     @PluginElement("Filters") filter: Filter): ElasticFutAppender = new ElasticFutAppender(name, layout, filter, ignoreExceptions, queueSize, bulkSize, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType)
}

//TODO Check extra document creation 12k+ vs 10K
@Plugin(name = "ElasticFut", category = "Core", elementType = "appender", printObject = true)
class ElasticFutAppender(name: String, layout: Layout[_ <: Serializable], filter: Filter, ignoreExceptions: Boolean, queueSize: Int, bulkSize: Int, hosts: String, portNo: String, cluster: String, indexNamePrefix: String, indexNameDateFormat: String, indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {
  import scala.concurrent.ExecutionContext.Implicits.global

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
        override def port: Int = Integer.parseInt(portNo)
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
      case e: ElasticLayout =>
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



