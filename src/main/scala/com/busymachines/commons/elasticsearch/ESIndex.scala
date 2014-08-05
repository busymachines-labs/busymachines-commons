package com.busymachines.commons.elasticsearch

import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.concurrent.TrieMap
import scala.language.postfixOps

import org.elasticsearch.Version
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest

import com.busymachines.commons.Logging
import com.busymachines.commons.event.EventBus

class ESIndex(val config: ESConfig, val indexName : String, _eventBus: => EventBus) extends Logging {
  def this(config : ESConfig, eventBus: => EventBus) = this(config, config.indexName, eventBus)

  private val nrOfShards = config.numberOfShards
  private val nrOfReplicas = config.numberOfReplicas
  private val initializeHandlers = TrieMap[() => Unit, Unit]()
  private val initialized = new AtomicBoolean(false)

  private val client0 = 
    ESClient(config)
  
  lazy val client =
    initialize(client0)

  def eventBus = _eventBus

  def onInitialize(handler : () => Unit) {
    if (initialized.get)
      handler()
    initializeHandlers.put(handler, {})
  }

  def refresh() {
    client.javaClient.admin.indices().refresh(new RefreshRequest()).actionGet
  }

  def addMapping(typeName: String, mapping: ESMapping[_]) =
    client.addMapping(indexName, typeName, mapping)

  def drop() {
    initialized.set(false)
    client0.dropIndex(indexName)
  }

  private def initialize(client: ESClient): ESClient = {
    if (!client.indexExists(indexName))
      client.createIndex(indexName)
    initialized.set(true)
    // call initialize handlers
    for ((handler, _) <- initializeHandlers) 
    	handler()
    client
  }
}