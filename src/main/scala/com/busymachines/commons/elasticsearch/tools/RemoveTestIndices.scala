package com.busymachines.commons.elasticsearch.tools

import com.busymachines.commons.elasticsearch.{ESConfig, ESClient}

object RemoveTestIndices extends App {

  val config = new ESConfig("") {
    override val clusterName = "elasticsearch"
    override val hostNames = Seq("Localhost")
    override val port = 9301
  }

  val client = ESClient(config)
  for (indexName <- client.listIndexNames if indexName.startsWith("test-"))
    client.dropIndex(indexName)
}
