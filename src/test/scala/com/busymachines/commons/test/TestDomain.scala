package com.busymachines.commons.test

import com.busymachines.commons.domain.CommonJsonFormats
import spray.json._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESMapping
import org.joda.time.DateTime
import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.domain.GeoPoint

case class PropertyExternalReference(
  id: Id[PropertyExternalReference] = Id.generate[PropertyExternalReference],
  name: String) extends HasId[PropertyExternalReference]

case class Property(
  id: Id[Property] = Id.generate[Property],
  likes : Option[Int] = None,  
  mandatory: Boolean = false,
  name: String,
  value: Double = 0,
  externalReferences: List[PropertyExternalReference] = Nil) extends HasId[Property]

case class Item(
  id: Id[Item] = Id.generate[Item],
  location:GeoPoint,
  expectedProfit : Option[Int] = None,
  priceNormal : Double = 0,
  priceSale : Double = 0,
  validUntil: DateTime,
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object DomainJsonFormats extends CommonJsonFormats {
  implicit val propertyReferenceFormat = jsonFormat2(PropertyExternalReference)
  implicit val propertyFormat = jsonFormat6(Property)
  implicit val itemFormat = jsonFormat8(Item)
}

object PropertyReferenceMapping extends ESMapping[PropertyExternalReference] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & NotAnalyzed & IncludeInAll
}

object PropertyMapping extends ESMapping[Property] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val mandatory = "mandatory" as Boolean
  val name = "name" as String & NotAnalyzed & IncludeInAll
  val value = "value" as Double
  val likes = "likes" as Integer
  val externalReferences = "externalReferences" -> "external_references" as Nested(PropertyReferenceMapping)
}

object ItemMapping extends ESMapping[Item] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val location = "location" as GeoPoint
  val name = "name" as String & NotAnalyzed & IncludeInAll
  val priceNormal = "priceNormal" as Double
  val expectedProfit = "expectedProfit" as Double
  val priceSale = "priceSale" as Double
  val validUntil = "validUntil" as Date & NotAnalyzed & IncludeInAll
  val properties = "properties" -> "item_properties" as Nested(PropertyMapping)
}

