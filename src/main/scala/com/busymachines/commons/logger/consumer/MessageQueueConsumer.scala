package com.busymachines.commons.logger.consumer

import java.util.concurrent.LinkedBlockingQueue

import com.busymachines.commons.elasticsearch.{ESCollection, ESIndex}
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.logger.domain.{LoggerESTypes, LogMessage}
import com.busymachines.commons.testing.DefaultTestESConfig
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Matei on 15.08.2014.
 */
//TODO Try Bulk API / find a way to flush all messages from queue before shut down
class MessageQueueConsumer(messageQueue: LinkedBlockingQueue[LogMessage],clusterName:String,
                           hostNames:String, port:String, indexNamePrefix:String, indexNameDateFormat:String,
                           indexDocumentType:String) extends Runnable {

  lazy val actualIndexName = s"${indexNamePrefix}-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  lazy val collection = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val index = new ESIndex(DefaultTestESConfig, actualIndexName, DoNothingEventSystem)
    val collection = new ESCollection[LogMessage](index, LoggerESTypes.LogMessage)
    collection
  }
import com.busymachines.commons.Implicits._
  def run() {
    do {
      try{
        collection.create(messageQueue.take(), false, None)
      }catch{
        case ex:Exception=>println(ex)
      }
    }while(true)
  }

}
