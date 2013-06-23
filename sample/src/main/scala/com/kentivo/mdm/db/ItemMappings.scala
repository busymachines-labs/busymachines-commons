package com.kentivo.mdm.db

import org.elasticsearch.client.Client
import com.busymachines.commons.dao.elasticsearch.Mapping
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyValue
import com.kentivo.mdm.domain.Source
import com.busymachines.commons.dao.elasticsearch.Index
import org.elasticsearch.node.NodeBuilder.nodeBuilder

class MdmIndex extends Index(nodeBuilder.client(true).node.client)

object ItemMapping extends Mapping[Item] {
  val repository = "repository" as String & Stored & NotAnalyzed
  val mutation = "mutation" as String & Stored & NotAnalyzed
  val id = "_id" -> "id" as String & Stored & NotAnalyzed
  val owner = "owner" as String & Stored & NotAnalyzed
  val name = "name" as String & Stored & Analyzed
  val parent = "parents" as String & Stored & NotAnalyzed
  val properties = "properties" as Nested(PropertyMapping) & Stored 
  val values = "values" as Nested(PropertyValueMapping) & Stored 
}

object PropertyMapping extends Mapping[Property] {
  val repository = "repository" as String & Stored & NotAnalyzed
  val mutation = "mutation" as String & Stored & NotAnalyzed
  val id = "_id" -> "id" as String & Stored & NotAnalyzed
  val name = "name" as String & Stored & Analyzed
  val scope = "scope" as String & Stored & NotAnalyzed
  val `type` = "type" as String & Stored & NotAnalyzed
  val defaultUnit = "defaultUnit" as String & Stored & NotAnalyzed
  val itemValueBase = "itemValueBase" as String & Stored & NotAnalyzed
  val mandatory = "mandatory" as Boolean & Stored & NotAnalyzed
  val multiValue = "multiValue" as Boolean & Stored & NotAnalyzed
  val multiLingual = "multiLingual" as Boolean & Stored & NotAnalyzed
  val groups = "groups" as String & Stored & NotAnalyzed
  val rules = "rules" as String & Stored & NotAnalyzed
}

object PropertyValueMapping extends Mapping[PropertyValue] {
  val property = "property" as String & Stored & NotAnalyzed
  val mutation = "mutation" as String & Stored & NotAnalyzed
  val value = "value" as String & Stored & Analyzed
  val locale = "locale" as String & Stored & NotAnalyzed
  val unit = "unit" as String & Stored & NotAnalyzed
}

object SourceMapping extends Mapping[Source] {
  val id = "_id" -> "id" as String & Stored & NotAnalyzed
  val name = "name" as String & Stored & Analyzed
  val repository = "repository" as String & Stored & NotAnalyzed
  val model = "model" as Nested(ItemMapping) & Stored 
}

