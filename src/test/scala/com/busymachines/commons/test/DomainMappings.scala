package com.busymachines.commons.test
import com.busymachines.commons.dao.elasticsearch.Index
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import com.busymachines.commons.dao.elasticsearch.Mapping
import org.elasticsearch.client.Client

class TestESIndex(client:Client) extends Index(client) {
  val name = "busymachines-commons.test"
  val nrOfShards = 5
  val nrOfReplicas = 1
}

object PropertyMapping extends Mapping[Property] {
  val id = "id" -> "_id" as String & NotAnalyzed  
  val mandatory = "mandatory" as Boolean
  val name = "name" as String & NotAnalyzed	  
}

object ItemMapping extends Mapping[Item] {
  val id = "id" -> "_id" as String & NotAnalyzed  
  val name = "name" as String & NotAnalyzed
  val properties = "item_properties" as Nested(PropertyMapping)
}