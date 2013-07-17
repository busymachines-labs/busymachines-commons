package com.busymachines.commons.test

import com.busymachines.commons.domain.CommonJsonFormats
import spray.json._
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id

case class Property(
  id: Id[Property] = Id.generate[Property],
  mandatory: Boolean = false,
  name: String) extends HasId[Property]

case class Item(
  id: Id[Item] = Id.generate[Item],
  name: String,
  properties: List[Property] = Nil) extends HasId[Item]

object DomainJsonFormats extends DomainJsonFormats

trait DomainJsonFormats extends CommonJsonFormats {
  implicit val propertyFormat = jsonFormat3(Property)
  implicit val itemFormat = jsonFormat3(Item)
}
