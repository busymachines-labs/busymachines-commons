package com.busymachines.commons.elasticsearch

import scala.collection.mutable.Buffer
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import scala.collection.concurrent.TrieMap
import java.util.concurrent.atomic.AtomicBoolean
import com.busymachines.commons.event.EventBus
import com.busymachines.commons.event.BusEvent
import com.busymachines.commons.event.LocalEventBus
import akka.actor.ActorSystem
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest

class ESIndex(_client: ESClient, val name : String,eventBus:EventBus) {

  type InitializeHandler = () => Unit
  
  private val nrOfShards = _client.config.numberOfShards
  private val nrOfReplicas = _client.config.numberOfShards
  private val initializeHandlers = TrieMap[InitializeHandler, Unit]()
  private var initialized = new AtomicBoolean(false)
  
  lazy val bus:EventBus = eventBus match {
    case null =>  new LocalEventBus(ActorSystem("bm")).asInstanceOf[EventBus] 
    case _ => eventBus
  }
  
  lazy val client = {
    initialize
    _client
  }

  def refresh {
    _client.admin.indices().refresh(new RefreshRequest())
  }
  
  def drop {
    initialized.set(false)
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (exists) {
      _client.admin().indices().delete(new DeleteIndexRequest(name)).get()
    }
  }

  def initialize {
    println("init")
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (!exists) {
      _client.admin.indices.create(new CreateIndexRequest(name).settings(
        s"""
           {
            "number_of_shards" : $nrOfShards,
            "number_of_replicas" : $nrOfReplicas,
            "index.mapper.dynamic": false            
          }
          """)).get()
      Await.ready(indicesExistsReponse, 10 seconds)
    }
    initialized.set(true)
    // call initialize handlers
    for ((handler, _) <- initializeHandlers) 
    	handler()
  }
  
  def onInitialize(handler : () => Unit) {
    if (initialized.get) 
      handler()
    initializeHandlers.put(handler, {})
  }
}