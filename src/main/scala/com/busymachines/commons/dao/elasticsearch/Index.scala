package com.busymachines.commons.dao.elasticsearch

import scala.collection.mutable.Buffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.client.Client
import org.scalastuff.esclient.ESClient
import org.elasticsearch.node.NodeBuilder.nodeBuilder
abstract class Index(_client : Client) {

  val name : String
  val nrOfShards : Int
  val nrOfReplicas : Int
  
  lazy val client = {
    initialize
    _client
  }
  
  def initialize {
    val indicesExistsReponse = _client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 10 seconds).isExists
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