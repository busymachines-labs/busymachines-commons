package com.busymachines.commons.test

import org.scalatest.Finders
import org.scalatest.FlatSpec

import com.busymachines.commons.domain.CommonJsonFormats
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.commons.elasticsearch.implicits.richJsValue

import spray.json.pimpAny

class RichJsValueTest extends FlatSpec with CommonJsonFormats {

  case class Property(
    id: Id[Property] = Id.generate[Property],
    mandatory: Boolean = false,
    name: String,
    tagCounts: Map[String, String] = Map.empty) extends HasId[Property]

  case class Item(
    id: Id[Item] = Id.generate[Item],
    name: String,
    properties: List[Property] = Nil) extends HasId[Item]

  object PropertyMapping extends ESMapping[Property] {
    val id = "id" -> "_id" as String & NotAnalyzed
    val mandatory = "mandatory" as Boolean
    val name = "name" as String & NotAnalyzed
    val tagCounts = "tagCounts" -> "tag_counts" as Object[Map[String, String]] & NotAnalyzed
  }

  object ItemMapping extends ESMapping[Item] {
    val id = "id" -> "_id" as String & NotAnalyzed
    val name = "name" as String & NotAnalyzed
    val properties = "properties" -> "item_properties" as Nested(PropertyMapping)
  }

  implicit val propertyFormat = jsonFormat4(Property)
  implicit val itemFormat = jsonFormat3(Item)

  val itemId = Id[Item]("64e9d0ee-3954-4d73-acfe-8237e01e2090")
  val peopertyId1 = Id[Property]("64e9d0ee-3954-4d73-acfe-8237e01e2091")
  val peopertyId2 = Id[Property]("64e9d0ee-3954-4d73-acfe-8237e01e2092")

  "RichJSValue" should "map fields of one non-nested object according to the mapping" in {
    val entity = Item(id = itemId, name = "item1").toJson.mapToES(ItemMapping)
    assert(entity.toString === """{"_id":"64e9d0ee-3954-4d73-acfe-8237e01e2090","name":"item1","item_properties":[]}""")
  }

  it should "map fields for nested objects according to the mapping" in {
    val entity = Item(id = itemId, name = "item1", properties =
      Property(id = peopertyId1, mandatory = true, name = "p1") :: Property(id = peopertyId2, mandatory = false, name = "p2") :: Nil).toJson.mapToES(ItemMapping)
    assert(entity.toString === """{"_id":"64e9d0ee-3954-4d73-acfe-8237e01e2090","name":"item1","item_properties":[{"id":"64e9d0ee-3954-4d73-acfe-8237e01e2091","mandatory":true,"name":"p1","tagCounts":{}},{"id":"64e9d0ee-3954-4d73-acfe-8237e01e2092","mandatory":false,"name":"p2","tagCounts":{}}]}""")
  }

  it should "map fields for nested objects with map type value according to the mapping" in {
    val entity = Item(id = itemId, name = "item1", properties =
      Property(id = peopertyId1, mandatory = true, name = "p1", tagCounts = Map("p11" -> "1", "p12" -> "2")) :: Property(id = peopertyId2, mandatory = false, name = "p2", tagCounts = Map("p21" -> "1", "p22" -> "2")) :: Nil).toJson.mapToES(ItemMapping)
    assert(entity.toString === """{"_id":"64e9d0ee-3954-4d73-acfe-8237e01e2090","name":"item1","item_properties":[{"id":"64e9d0ee-3954-4d73-acfe-8237e01e2091","mandatory":true,"name":"p1","tagCounts":{"p11":"1","p12":"2"}},{"id":"64e9d0ee-3954-4d73-acfe-8237e01e2092","mandatory":false,"name":"p2","tagCounts":{"p21":"1","p22":"2"}}]}""")
  }

}