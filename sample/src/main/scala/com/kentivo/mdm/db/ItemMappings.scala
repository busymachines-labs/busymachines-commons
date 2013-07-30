package com.kentivo.mdm.db

import org.elasticsearch.client.Client
import com.busymachines.commons.elasticsearch.ESMapping
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyValue
import com.kentivo.mdm.domain.Source
import com.busymachines.commons.elasticsearch.ESIndex
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import java.util.Locale


object ItemMapping extends ESMapping[Item] {
  val repository = "repository" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val id = "id" -> "_id" as String & NotAnalyzed
  val owner = "owner" as String & NotAnalyzed
  val name = "name" as Object[Map[Locale, String]] & Analyzed
  val parent = "parents" as String & NotAnalyzed
  val properties = "properties" as Nested(PropertyMapping) 
  val values = "values" as Nested(PropertyValueMapping) 
}

object PropertyMapping extends ESMapping[Property] {
  val repository = "repository" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as Object[Map[Locale, String]] & Analyzed
  val scope = "scope" as String & NotAnalyzed
  val `type` = "type" as String & NotAnalyzed
  val defaultUnit = "defaultUnit" as String & NotAnalyzed
  val itemValueBase = "itemValueBase" as String & NotAnalyzed
  val mandatory = "mandatory" as Boolean & NotAnalyzed
  val multiValue = "multiValue" as Boolean & NotAnalyzed
  val multiLingual = "multiLingual" as Boolean & NotAnalyzed
  val groups = "groups" as String & NotAnalyzed
  val rules = "rules" as String & NotAnalyzed
}

object PropertyValueMapping extends ESMapping[PropertyValue] {
  val property = "property" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val value = "value" as String & Analyzed
  val locale = "locale" as String & NotAnalyzed
  val unit = "unit" as String & NotAnalyzed
}

object SourceMapping extends ESMapping[Source] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val repository = "repository" as String & NotAnalyzed
  val model = "model" as Nested(ItemMapping)
}
