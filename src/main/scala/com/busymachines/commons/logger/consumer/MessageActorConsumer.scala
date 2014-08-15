package com.busymachines.commons.logger.consumer

import akka.actor.Actor
import com.busymachines.commons.elasticsearch.ESConfig
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


/**
 * Created by Alexandru Matei on 15.08.2014.
 */

//TODO Add latest actors
//class MessageActorConsumer (clusterName:String, hostNames:String, port:String, indexNamePrefix:String, indexNameDateFormat:String,
//                             indexDocumentType:String) extends Actor {
//
//  def act ={
//
//  }
//  def receive: Unit ={
//
//  }
  /*lazy val client = new TransportClient (ImmutableSettings.settingsBuilder ().put ("cluster.name", clusterName))
    .addTransportAddresses (hostNames.split (",").map (new InetSocketTransportAddress (_, port.toInt)): _*)

  lazy val actualIndexName = s"${indexNamePrefix}-${DateTimeFormat.forPattern (indexNameDateFormat).print (DateTime.now)}"

  def act =
    loop {
      react {
        case message:String =>
          try {
            client.prepareIndex (
              actualIndexName, indexDocumentType)
              .setSource(message)
              .execute ()
              .actionGet ()
          } catch {
            case ex: Exception => println (ex)
          }
        case _ =>
      }
    }*/
//}
