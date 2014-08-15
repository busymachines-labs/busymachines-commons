package com.busymachines.commons.logger.consumer

import java.util.concurrent.LinkedBlockingQueue

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Alexandru Matei on 15.08.2014.
 */
class MessageQueueConsumer(messageQueue: LinkedBlockingQueue[String],clusterName:String,
                           hostNames:String, port:String, indexNamePrefix:String, indexNameDateFormat:String,
                           indexDocumentType:String) extends Runnable {

  lazy val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", clusterName))
    .addTransportAddresses(hostNames.split(",").map(new InetSocketTransportAddress(_, port.toInt)): _*)

  lazy val actualIndexName = s"${indexNamePrefix}-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  def run() {
    do {
      try{
      client.prepareIndex(
        actualIndexName, indexDocumentType)
        .setSource(messageQueue.take)
        .execute()
        .actionGet()
      }catch{
        case ex:Exception=>println(ex)
      }
    }while(true)
  }
}
