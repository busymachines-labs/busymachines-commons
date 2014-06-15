package com.busymachines.commons.elasticsearch

import com.busymachines.commons.event.EventBus
import java.util.concurrent.atomic.AtomicBoolean
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import com.busymachines.commons.Logging
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.Version
import org.scalastuff.esclient.ESClient

private object ESIndex {
  private val clientsByClusterName = TrieMap[String, ESClient]()
}

class ESIndex(config: ESConfig, val name : String, _eventBus: => EventBus) extends Logging {
  def this(config : ESConfig, eventBus: => EventBus) = this(config, config.indexName, eventBus)

  private val nrOfShards = config.numberOfShards
  private val nrOfReplicas = config.numberOfShards
  private val initializeHandlers = TrieMap[() => Unit, Unit]()
  private val initialized = new AtomicBoolean(false)

  private lazy val client0 = {
    ESIndex.clientsByClusterName.getOrElseUpdate(config.clusterName, {
      info("Using ElasticSearch client " + Version.CURRENT)
      new ESClient(
        new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
          addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
        })
    })
  }

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

  def addMapping(typeName: String, mapping: ESMapping[_]) {
    val mappingConfiguration = mapping.mappingDefinition(typeName).toString
    try {
      debug(s"Schema for $name/$typeName: $mappingConfiguration")
      client.javaClient.admin.indices.putMapping(new PutMappingRequest(name).`type`(typeName).source(mappingConfiguration)).get()
    }
    catch {
      case e : Throwable =>
        val msg = s"Invalid schema for $name/$typeName: ${e.getMessage} in $mappingConfiguration"
        error(msg, e)
        throw new Exception(msg, e)
    }
  }

  def drop() {
    initialized.set(false)
    val indicesExistsReponse = client0.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (exists) {
      client0.javaClient.admin.indices().delete(new DeleteIndexRequest(name)).get()
    }
  }

  private def initialize(client: ESClient): ESClient = {
    val indicesExistsReponse = client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (!exists) {
      client.javaClient.admin.indices.create(new CreateIndexRequest(name).settings(
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
    client
  }
}