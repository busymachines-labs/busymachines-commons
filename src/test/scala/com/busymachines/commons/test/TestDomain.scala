package com.busymachines.commons.test

import com.busymachines.commons.domain.CommonJsonFormats
import spray.json._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESMapping
import org.joda.time.DateTime

case class PropertyExternalReference(
  id: Id[PropertyExternalReference] = Id.generate[PropertyExternalReference],
  name: String) extends HasId[PropertyExternalReference]

case class Property(
  id: Id[Property] = Id.generate[Property],
  mandatory: Boolean = false,
  name: String,
  externalReferences: List[PropertyExternalReference] = Nil) extends HasId[Property]

case class Item(
  id: Id[Item] = Id.generate[Item],
  validUntil: DateTime,
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object DomainJsonFormats extends CommonJsonFormats {
  implicit val propertyReferenceFormat = jsonFormat2(PropertyExternalReference)
  implicit val propertyFormat = jsonFormat4(Property)
  implicit val itemFormat = jsonFormat4(Item)
}

object PropertyReferenceMapping extends ESMapping[PropertyExternalReference] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & NotAnalyzed
}

object PropertyMapping extends ESMapping[Property] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val mandatory = "mandatory" as Boolean
  val name = "name" as String & NotAnalyzed
  val externalReferences = "externalReferences" -> "external_references" as Nested(PropertyReferenceMapping)
}

object ItemMapping extends ESMapping[Item] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & NotAnalyzed
  val validUntil = "validUntil" as Date & NotAnalyzed
  val properties = "properties" -> "item_properties" as Nested(PropertyMapping)
}

