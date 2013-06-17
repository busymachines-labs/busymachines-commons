package com.kentivo.mdm.commons

import org.elasticsearch.client.Client
import org.scalastuff.esclient.ESClient
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import scala.concurrent.Await
import scala.concurrent.duration._

class ESSourceProvider {

  val ITEM = new ESSource(this, "kentivo", "item")
  val SOURCE = new ESSource(this, "kentivo", "source")
  val MUTATION = new ESSource(this, "kentivo", "mutation")
  val MEDIA = new ESSource(this, "kentivo-media", "media")

  lazy val client: Client = {
    val client = nodeBuilder.client(true).node.client
    createIndex(client, "kentivo")
    createIndex(client, "kentivo-media")
    client
  }
  
  private def createIndex(client : Client, name : String) = {
    val indicesExistsReponse = client.execute(new IndicesExistsRequest(name))
    val exists = Await.result(indicesExistsReponse, 10 seconds).isExists
    if (!exists) {
      val createindexReponse = client.execute(new CreateIndexRequest(name)) 
      Await.ready(indicesExistsReponse, 10 seconds)
    }
  }
}

class ESSource private[commons] (provider : ESSourceProvider, val index : String, val doctype : String) {
  val client = ESClient(provider.client)
}