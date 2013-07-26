package com.busymachines.commons.test

import com.busymachines.commons.domain.CommonJsonFormats
import spray.json._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESMapping

case class Property(
  id: Id[Property] = Id.generate[Property],
  mandatory: Boolean = false,
  name: String) extends HasId[Property]

case class Item(
  id: Id[Item] = Id.generate[Item],
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object DomainJsonFormats extends CommonJsonFormats {
  implicit val propertyFormat = jsonFormat3(Property)
  implicit val itemFormat = jsonFormat3(Item)
}

object PropertyMapping extends ESMapping[Property] {
  val id = "id" -> "_id" as String & NotAnalyzed  
  val mandatory = "mandatory" as Boolean
  val name = "name" as String & NotAnalyzed   
}

object ItemMapping extends ESMapping[Item] {
  val id = "id" -> "_id" as String & NotAnalyzed  
  val name = "name" as String & NotAnalyzed
  val properties = "item_properties" as Nested(PropertyMapping)
}