package com.busymachines.commons.elasticsearch

import org.scalastuff.esclient.ActionMagnet
import scala.collection.concurrent.TrieMap
import org.elasticsearch.client.Client
import scala.concurrent.Future
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import scala.concurrent.Await
import scala.concurrent.duration._
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import com.busymachines.commons.Logging
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest

object ESClient {
  private val clientsByClusterName = TrieMap[String, ESClient]()
  
  def apply(config: ESConfig) = 
    clientsByClusterName.getOrElseUpdate(config.clusterName, {
      new ESClient(
        new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
          addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
        }, config)
    })
}

class ESClient(val javaClient: Client, val config: ESConfig) extends Logging {
  
  def execute[Request, Response](request: Request)(implicit action: ActionMagnet[Request, Response]): Future[Response] =
    action.execute(javaClient, request)

  def executeSync[Request, Response](request: Request)(implicit action: ActionMagnet[Request, Response]): Response =
    Await.result(action.execute(javaClient, request), 30 seconds)

  def indexExists(indexName: String): Boolean =  
    executeSync(new IndicesExistsRequest(indexName)).isExists

  def createIndex(indexName: String) {
    executeSync(new CreateIndexRequest(indexName).settings(
    s"""
       {
        "number_of_shards" : ${config.numberOfShards},
        "number_of_replicas" : ${config.numberOfReplicas},
        "index.mapper.dynamic": false            
      }
      """))
  }
    
  def addMapping(indexName: String, typeName: String, mapping: ESMapping[_]) {
    val mappingConfiguration = mapping.mappingDefinition(typeName).toString
    try {
      debug(s"Schema for $indexName/$typeName: $mappingConfiguration")
      executeSync(new PutMappingRequest(indexName).`type`(typeName).source(mappingConfiguration))
    }
    catch {
      case e : Throwable =>
        val msg = s"Invalid schema for $indexName/$typeName: ${e.getMessage} in $mappingConfiguration"
        error(msg, e)
        throw new Exception(msg, e)
    }
  }
}