package com.busymachines.commons.test

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch2.ESMapping
import org.joda.time.DateTime
import com.busymachines.commons.domain.GeoPoint
import scala.reflect.ClassTag
import com.busymachines.commons.implicits._
import com.busymachines.commons.spray._
import spray.json.JsValue
import spray.json.{ JsString, JsonFormat }

import com.busymachines.commons.domain.CommonJsonFormats

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

case class Item(
  id: Id[Item] = Id.generate[Item],
  location: GeoPoint,
  expectedProfit: Option[Int] = None,
  priceNormal: Double = 0,
  priceSale: Double = 0,
  validUntil: DateTime,
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object Mappings extends CommonJsonFormats {
  implicit val propertyReferenceFormat = format2(PropertyExternalReference)
  implicit val propertyFormat = format6(Property)
  implicit val itemFormat = format8(Item)

  object PropertyExternalReferenceMapping extends ESMapping[PropertyExternalReference] {
    val id = "_id" -> "id" :: String.as[Id[PropertyExternalReference]] & NotAnalyzed
    val name = "name" :: String & NotAnalyzed
  }

  object PropertyMapping extends ESMapping[Property] {
    val id = "_id" -> "id" :: String.as[Id[Property]] & NotAnalyzed
    val mandatory = "mandatory" :: Boolean
    val name = "name" :: String & NotAnalyzed
    val value = "value" :: Double
    val likes = "likes" :: Integer
    val externalReferences = "externalReferences" :: Nested(PropertyExternalReferenceMapping)
  }

  object ItemMapping extends ESMapping[Item] {
    val id = "_id" -> "id" :: String.as[Id[Item]] & NotAnalyzed
    val location = "location" :: GeoPoint
    val name = "name" :: String & NotAnalyzed
    val priceNormal = "priceNormal" :: Double
    val expectedProfit = "expectedProfit" :: Double
    val priceSale = "priceSale" :: Double
    val validUntil = "validUntil" :: Date & NotAnalyzed
    val properties = "item_properties" -> "properties" :: Nested(PropertyMapping)
  }

}




