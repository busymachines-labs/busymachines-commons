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

class ESIndex(_client: ESClient, val name : String) {

  private val nrOfShards = _client.config.numberOfShards
  private val nrOfReplicas = _client.config.numberOfShards

  lazy val client = {
    initialize
    _client
  }

  def drop {
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (exists) {
      _client.admin().indices().delete(new DeleteIndexRequest(name)).get()
    }
  }

  def initialize {
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 30 seconds).isExists
    if (!exists) {
      _client.admin.indices.create(new CreateIndexRequest(name).settings(
        s"""
           {
            "number_of_shards" : $nrOfShards,
            "number_of_replicas" : $nrOfReplicas
          }
          """)).get()
      Await.ready(indicesExistsReponse, 10 seconds)
    }
  }
}