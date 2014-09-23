package com.busymachines.commons.logging.appender

import java.util.concurrent.LinkedBlockingQueue

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest

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

import com.busymachines.commons.elasticsearch.{ESMapping, ESConfig}
import com.busymachines.commons.logging.{LoggingJsonFormats, ESLayout}
import com.busymachines.commons.logging.domain.{IndexSelector, LogMessageESMappings, LogMessage}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alexandru Matei on 14.08.2014.
 */

object ESAppender {
  @PluginFactory
  def createAppender(@PluginAttribute("name") name: String,
    @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
    @PluginAttribute("queueSize") queueSize: Int,
    @PluginAttribute("bulkSize") bulkSize: Int,
    //sleep time is in milliseconds
    @PluginAttribute(value = "sleepTime", defaultInt = 1000) sleepTime: Int,
    @PluginAttribute(value = "hostNames", defaultString = "localhost") hosts: String,
    @PluginAttribute(value = "port", defaultInt = 9300) port: Int,
    @PluginAttribute(value = "clusterName", defaultString = "elasticsearch") clusterName: String,
    @PluginAttribute(value = "indexNamePrefix", defaultString = "logstash") indexNamePrefix: String,
    @PluginAttribute(value = "indexNameDateFormat", defaultString = "yyyy.MM.dd") indexNameDateFormat: String,
    @PluginAttribute(value = "indexDocumentType", defaultString = "log") indexDocumentType: String,
    @PluginAttribute(value = "shardsNo", defaultInt = 1) shardsNo: Int,
    @PluginAttribute(value = "replicasNo", defaultInt = 0) replicasNo: Int,
    @PluginElement("Layout") layout: Layout[_ <: Serializable],
    @PluginElement("Filters") filter: Filter): ESAppender = new ESAppender(name, layout, filter, ignoreExceptions, queueSize, bulkSize, sleepTime, hosts, port, clusterName, indexNamePrefix, indexNameDateFormat, indexDocumentType)
}

@Plugin(name = "ESAppender", category = "Core", elementType = "appender", printObject = true)
class ESAppender(
  name: String,
  layout: Layout[_ <: Serializable],
  filter: Filter,
  ignoreExceptions: Boolean,
  queueSize: Int,
  bulkSize: Int,
  sleepTime: Int,
  hosts: String,
  portNo: Int,
  cluster: String,
  indexNamePrefix: String,
  indexNameDateFormat: String,
  indexDocumentType: String) extends AbstractAppender(name, filter, layout, ignoreExceptions) with LoggingJsonFormats{

  private lazy val messageQueue = new LinkedBlockingQueue[LogMessage](queueSize)
  lazy val actualIndexName = new IndexSelector(indexNamePrefix,indexNameDateFormat,DateTime.now)
  lazy val config = new ESConfig("") {
    override def clusterName: String = cluster
    override def indexName: String = actualIndexName.getName
    override def hostNames: Seq[String] = hosts.split(",")
    override def numberOfShards: Int = 1
    override def numberOfReplicas: Int = 0
    override def port: Int = portNo
  }
  lazy val javaClient = {
    new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
      addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
    }
  }

  def bulk(list: Seq[LogMessage]): Unit = {
    if (actualIndexName.isUpdated)
      initIndex()
    val bulkRequest = javaClient.prepareBulk()
    list.foreach(o => bulkRequest.add(javaClient.
      prepareIndex(actualIndexName.getName, indexDocumentType).
      setSource(logMessageFormat.write(o).toString)))
    bulkRequest.execute()
  }

  def initIndex() {
    //shameless duplication from ESClient
    def executeSync[Request, Response](request: Request)(implicit action: ActionMagnet[Request, Response]): Response =
      Await.result(action.execute(javaClient, request), 30 seconds)

    def indexExists(indexName: String): Boolean =
      executeSync(new IndicesExistsRequest(indexName)).isExists

    def addMapping(indexName: String, typeName: String, mapping: ESMapping[_]) {
      val mappingConfiguration = mapping.mappingDefinition(typeName).toString
      try {
        //      debug(s"Schema for $indexName/$typeName: $mappingConfiguration")
        executeSync(new PutMappingRequest(indexName).`type`(typeName).source(mappingConfiguration))
      }
      catch {
        case e : Throwable =>
          val msg = s"Invalid schema for $indexName/$typeName: ${e.getMessage} in $mappingConfiguration"
          //        error(msg, e)
          throw new Exception(msg, e)
      }
    }
    def createIndex(indexName: String, typeName:String, mapping:ESMapping[_]) {
      executeSync(new CreateIndexRequest(indexName).settings(
        s"""
       {
        "number_of_shards" : ${config.numberOfShards},
        "number_of_replicas" : ${config.numberOfReplicas}
      }
      """))
      addMapping(indexName,typeName,mapping)
    }

    if (!indexExists(actualIndexName.getName))
      createIndex(actualIndexName.getName,indexDocumentType,LogMessageESMappings)
  }

  /**
   * This is basically Java right here. Looks horrible, don't judge.
   *
   * Used when the system is shutting down. Flushes anything that's left in the
   * message queue.
   */
  private def flush() {
    import scala.util.control.Breaks._
    var temp: LogMessage = null
    val listBuffer = ListBuffer[LogMessage]()
    while (!messageQueue.isEmpty()) {
      try {
        //just because at the while loop is wasn't empty doesn't mean
        //that by this time there actually is anything in the queue.
        //We do not have a guarantee that the process future has stopped executing
        //when we run this thing.
        temp = messageQueue.poll(1, TimeUnit.SECONDS)
        if (temp == null) {
          break
        } else {
          listBuffer += temp
        }
      } catch {
        case e: Throwable => break
      }
    }
    try {
      bulk(listBuffer)
    } catch {
      case e: Throwable => //ignore
    }
  }

  val isInterrupted = new AtomicBoolean(false)
  import scala.concurrent.ExecutionContext.Implicits.global

  def takeMultipleLogMessage(number: Int): ListBuffer[LogMessage] = {
    val listBuffer = ListBuffer[LogMessage]()
    (0 until number).foreach(i => {
      listBuffer += messageQueue.take()
    })
    listBuffer
  }

  val process = Future {
    import scala.util.control.Breaks._
    initIndex()
    while (!isInterrupted.get()) {
      if (messageQueue.size() >= bulkSize) {
        try {
          val listBuffer = takeMultipleLogMessage(bulkSize)
          bulk(listBuffer)
        } catch {
          case ex: Exception => println(ex)
        }
      } else {
        try {
          Thread.sleep(sleepTime)
          if (messageQueue.size() >= bulkSize) {
            //if it's larger we just continue the normal flow of the application
          } else {
            val listBuffer = takeMultipleLogMessage(messageQueue.size())
            bulk(listBuffer)
          }
        } catch {
          case e: Throwable => if (isInterrupted.get()) break
        }
      }
    }
  }

  override def stop() {
    isInterrupted.set(true)
    super.stop()
    flush()
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



