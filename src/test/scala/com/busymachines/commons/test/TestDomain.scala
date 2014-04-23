package com.busymachines.commons.test

import com.busymachines.commons.domain.CommonJsonFormats
import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import org.joda.time.DateTime
import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.commons.EnumValue
import com.busymachines.commons.AbstractEnum

case class PropertyExternalReference(
  id: Id[PropertyExternalReference] = Id.generate[PropertyExternalReference],
  name: String) extends HasId[PropertyExternalReference]

case class Property(
  id: Id[Property] = Id.generate[Property],
  likes: Option[Int] = None,
  mandatory: Boolean = false,
  name: String,
  value: Double = 0,
  externalReferences: List[PropertyExternalReference] = Nil) extends HasId[Property]

trait ItemType extends EnumValue[ItemType]
object ItemType extends AbstractEnum[ItemType] {
  case class Value(name: String, id: Int = nextId) extends Val(name) with ItemType
  val House = Value("house")
  val Car = Value("car")
  val Bicycle = Value("bicycle")
}


case class Item(
  id: Id[Item] = Id.generate[Item],
  `type` : Option[ItemType] = None,
  location: GeoPoint,
  expectedProfit: Option[Int] = None,
  priceNormal: Double = 0,
  priceSale: Double = 0,
  validUntil: DateTime,
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object Mappings extends CommonJsonFormats {
  implicit val itemTypeFormat = enumFormat(ItemType)
  implicit val propertyReferenceFormat = format2(PropertyExternalReference)
  implicit val propertyFormat = format6(Property)
  implicit val itemFormat = format9(Item)

  object PropertyExternalReferenceMapping extends ESMapping[PropertyExternalReference] {
    val id = "_id" -> "id" :: String.as[Id[PropertyExternalReference]]
    val name = "name" :: String
  }

  object PropertyMapping extends ESMapping[Property] {
    val id = "_id" -> "id" :: String.as[Id[Property]]
    val mandatory = "mandatory" :: Boolean
    val name = "name" :: String
    val value = "value" :: Double
    val likes = "likes" :: Integer
    val externalReferences = "externalReferences" :: Nested(PropertyExternalReferenceMapping)
  }

  object ItemMapping extends ESMapping[Item] {
    val id = "_id" -> "id" :: String.as[Id[Item]]
    val `type` = "type" :: String & NotAnalyzed
    val location = "location" :: GeoPoint
    val name = "name" :: String
    val priceNormal = "priceNormal" :: Double
    val expectedProfit = "expectedProfit" :: Double
    val priceSale = "priceSale" :: Double
    val validUntil = "validUntil" :: Date
    val properties = "item_properties" -> "properties" :: Nested(PropertyMapping)
  }

}




