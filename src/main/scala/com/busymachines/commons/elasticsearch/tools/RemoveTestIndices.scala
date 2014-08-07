package com.busymachines.commons.elasticsearch.tools

import com.busymachines.commons.elasticsearch.{ ESConfig, ESClient }
import com.busymachines.commons.testing.DefaultTestESConfig

object RemoveTestIndices extends App {

  val client = ESClient(DefaultTestESConfig)
  for (indexName <- client.listIndexNames if indexName.startsWith("test"))
    client.dropIndex(indexName)
}
