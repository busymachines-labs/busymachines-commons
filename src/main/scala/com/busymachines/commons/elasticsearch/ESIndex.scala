package com.busymachines.commons.elasticsearch

import akka.actor.ActorSystem
import com.busymachines.commons.event.EventBus
import com.busymachines.commons.event.LocalEventBus
import java.util.concurrent.atomic.AtomicBoolean
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class ESIndex(_client: ESClient, val name : String, eventBus:EventBus) {
  def this(config : ESConfig, name : String, eventBus:EventBus) = this(new ESClient(config), name, eventBus)
  def this(configBaseName : String, name : String, eventBus:EventBus) = this(new ESConfig(configBaseName), name, eventBus)

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

  def refresh() {
    _client.admin.indices().refresh(new RefreshRequest()).actionGet
  }
  
  def drop() {
    initialized.set(false)
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (exists) {
      _client.admin().indices().delete(new DeleteIndexRequest(name)).get()
    }
  }

  def initialize() {
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