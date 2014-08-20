package com.busymachines.commons.logger.appender

import java.util.concurrent.LinkedBlockingQueue

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.apache.logging.log4j.core.{ Filter, Layout, LogEvent }
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{ Plugin, PluginAttribute, PluginElement, PluginFactory }
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalastuff.esclient.ActionMagnet

import com.busymachines.commons.elasticsearch.ESConfig
import com.busymachines.commons.logger.ESLayout
import com.busymachines.commons.logger.domain.{ ExceptionLogJsonFormats, LogMessage }

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ESAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
    @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
    @PluginAttribute("queueSize") queueSize: Int,
    @PluginAttribute("bulkSize") bulkSize: Int,
    @PluginAttribute(value = "hostNames", defaultString = "localhost") hosts: String,
    @PluginAttribute(value = "port", defaultInt = 9300) port: Int,
    @PluginAttribute(value = "clusterName", defaultString = "elasticsearch") clusterName: String,
    @PluginAttribute(value = "indexNamePrefix", defaultString = "logstash") indexNamePrefix: String,
    @PluginAttribute(value = "indexNameDateFormat", defaultString = "yyyy.MM.dd") indexNameDateFormat: String,
    @PluginAttribute(value = "indexDocumentType", defaultString = "log") indexDocumentType: String,
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
  cluster: String,
  indexNamePrefix: String,
  indexNameDateFormat: String,
  indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) {

  lazy val config = new ESConfig("") {
    override def clusterName: String = "elasticsearch"
    override def indexName: String = "logger"
    override def hostNames: Seq[String] = Seq("localhost")
    override def numberOfShards: Int = 1
    override def numberOfReplicas: Int = 0
    override def port: Int = 9300
  }

  lazy val actualIndexName = s"$indexNamePrefix.${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  lazy val javaClient = {
    new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
      addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
    }
  }

  val typeName = "log"

  import ExceptionLogJsonFormats._
  def bulk(list: Seq[LogMessage]): Unit = {
    val bulkRequest = javaClient.prepareBulk()
    list.foreach(o => bulkRequest.add(javaClient.
      prepareIndex(actualIndexName, typeName).
      setSource(logMessageFormat.write(o).toString)))
    bulkRequest.execute().actionGet()
  }

  def initIndex() {
    //shameless duplication from ESClient
    def executeSync[Request, Response](request: Request)(implicit action: ActionMagnet[Request, Response]): Response =
      Await.result(action.execute(javaClient, request), 30 seconds)

    def indexExists(indexName: String): Boolean =
      executeSync(new IndicesExistsRequest(indexName)).isExists

    def createIndex(indexName: String) {
      executeSync(new CreateIndexRequest(indexName).settings(
        s"""
       {
        "number_of_shards" : ${config.numberOfShards},
        "number_of_replicas" : ${config.numberOfReplicas}
      }
      """))
    }

    if (!indexExists(actualIndexName))
      createIndex(actualIndexName)
  }

  lazy val messageQueue = new LinkedBlockingQueue[LogMessage](queueSize)
  import scala.concurrent.ExecutionContext.Implicits.global

  val process = Future {
    initIndex()
    println(s"bulkSize=${bulkSize}")
    while (true) {
      if (messageQueue.size() >= bulkSize) {
        try {
          val listBuffer: ListBuffer[LogMessage] = ListBuffer[LogMessage]()
          (0 until bulkSize).foreach(i => {
            listBuffer += messageQueue.take()
          })

          bulk(listBuffer)
        } catch {
          case ex: Exception => println(ex)
        }
      }
    }
  }

  override def append(event: LogEvent): Unit = {
    if (event.getSource().getClassName().contains("grizzled.slf4j.Logger"))
      return ;

    if (!(event.isInstanceOf[Log4jLogEvent]))
      return ;

    send(event)
  }

  def send(event: LogEvent) {
    getLayout match {
      case e: ESLayout =>
        try {
          messageQueue.put(e.toSerializable(event))
        } catch {
          case ex: Exception => println(s"Exception while appending to message queue ${ex.getMessage}")
        }
      case _ => println(s"Unsupported layout! $getLayout")
    }
  }
}



