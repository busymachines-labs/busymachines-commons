package com.busymachines.commons.test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import org.scalatest.FlatSpec
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.CommonJsonFormats._
import com.busymachines.commons.elasticsearch.ESSearchCriteria.Delegate
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.elasticsearch.ESSearchSort
import com.busymachines.commons.implicits.richFuture
import com.busymachines.commons.test.DomainJsonFormats.itemFormat
import com.busymachines.commons.test.DomainJsonFormats.propertyFormat
import com.busymachines.commons.testing.EmptyESTestIndex
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Page
import com.busymachines.commons.Logging
import com.busymachines.commons.event.DoNothingEventSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ItemDaoTests extends FlatSpec with Logging {

  val esIndex = new EmptyESTestIndex(getClass, new DoNothingEventSystem)
  val dao = new ESRootDao[Item](esIndex, ESType("item", ItemMapping))
  val nestedDao = new ESNestedDao[Item, Property]("properties") {
    def parentDao = dao

    def findEntity(item: Item, id: Id[Property]): Option[Property] =
      item.properties.find(_.id == id)

    def createEntity(item: Item, property: Property): Item =
      item.copy(properties = item.properties ++ List(property))

    def modifyEntity(item: Item, id: Id[Property], found: Found, modify: Property => Property): Item =
      item.copy(properties = item.properties.map {
        case property if property.id == id => found(modify(property))
        case property => property
      })

    def deleteEntity(item: Item, id: Id[Property], found: Found): Item =
      item.copy(properties = item.properties.filter {
        case property if property.id == id =>
          found(property); false
        case property => true
      })

    def retrieveParent(id: Id[Property]): scala.concurrent.Future[Option[Versioned[Item]]] =
      ???
  }

  val now = DateTime.now(DateTimeZone.UTC)
  val geoPoint = GeoPoint(10, 10)

  "ItemDao" should "create & retrieve" in {
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    assert(dao.retrieve(item.id).await.get.validUntil === now)
    assert(dao.retrieve(item.id).await.get.location === geoPoint)
  }

  it should "create & update & retrieve" in {
    val item = Item(name = "Sample item 1", validUntil = now, location = geoPoint, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    dao.modify(item.id)(_ => item.copy(name = "Sample item 2")).await
    assert(dao.retrieve(item.id).await.get.name === "Sample item 2")
    assert(dao.retrieve(item.id).await.get.validUntil === now)
    assert(dao.retrieve(item.id).await.get.location === geoPoint)
  }

  it should "create & delete" in {
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    assert(dao.retrieve(item.id).await.get.validUntil === now)
    assert(dao.retrieve(item.id).await.get.location === geoPoint)

    // TODO: Fix test
    dao.delete(item.id, true).await
    //assert(dao.retrieve(item.id).await === None)
  }

  it should "create & search for simple nested object" in {
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.properties / PropertyMapping.name equ "Property3").await.size === 1)
  }

  it should "search by id" in {
    val propertyId = Id.generate[Property]
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(id = propertyId, name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.id equ item.id).await.size === 1)
    assert(dao.searchSingle(ItemMapping.properties / PropertyMapping.id equ propertyId).await.get.name === item.name)
  }

  it should "search by id using in" in {
    val propertyId = Id.generate[Property]
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(id = propertyId, name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.id in item.id :: Nil).await.size === 1)
    assert(dao.searchSingle(ItemMapping.properties / PropertyMapping.id in propertyId :: Nil).await.get.name === item.name)
  }

  it should "search with and/or" in {
    val item1 = Item(name = "AndOr", priceNormal = 4.0, priceSale = 3.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "AndOr", priceNormal = 2.0, priceSale = 1.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item1, true).await
    dao.create(item2, true).await
    assert(dao.search((ItemMapping.name equ "AndOr") and (ItemMapping.priceSale equ 1.0)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "AndOr") and ((ItemMapping.priceSale equ 3.0) or (ItemMapping.priceSale equ 1.0))).await.size === 2)
  }

  it should "search & paginate" in {
    val item1 = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item4 = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item5 = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await
    dao.create(item4, true).await
    dao.create(item5, true).await

    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(0, 1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(1, 1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(2, 1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(3, 1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(4, 1)).await.size === 1)

    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(0, 3)).await.size === 3)
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page(0, 5)).await.size === 5)

  }

  it should "search & sort" in {
    val item1 = Item(name = "D Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "C Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "B Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item4 = Item(name = "A Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item5 = Item(name = "E Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await
    dao.create(item4, true).await
    dao.create(item5, true).await

    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page.all, ESSearchSort.asc("name")).await.result(0).name === "A Sample item")
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page.all, ESSearchSort.asc("name")).await.result(1).name === "B Sample item")
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page.all, ESSearchSort.asc("name")).await.result(2).name === "C Sample item")
    assert(dao.search(ItemMapping.id in item1.id :: item2.id :: item3.id :: item4.id :: item5.id :: Nil, Page.all, ESSearchSort.asc("name")).await.result(3).name === "D Sample item")

  }

  it should "search with value based gt/gte/lt/lte" in {
    val item1 = Item(name = "MySpecial", priceNormal = 1.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "MySpecial", priceNormal = 2.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item1, true).await
    dao.create(item2, true).await

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal equ 0.0)).await.size === 0)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal equ 2.0)).await.size === 1)

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal neq 1.0)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal neq 3.0)).await.size === 2)

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gt 0.0)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gt 1.0)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gt 2.0)).await.size === 0)

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gte 0.0)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gte 1.0)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal gte 2.0)).await.size === 1)

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lt 0.0)).await.size === 0)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lt 1.0)).await.size === 0)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lt 2.0)).await.size === 1)

    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lte 0.0)).await.size === 0)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lte 1.0)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "MySpecial") and (ItemMapping.priceNormal lte 2.0)).await.size === 2)

  }

  it should "search with field based gt/gte/lt/lte" in {
    val item1 = Item(name = "321312 - Sample item", priceSale = 0.5, priceNormal = 1.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "321312 - Sample item", priceSale = 3.0, priceNormal = 4.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "321312 - Sample item", priceSale = 6.0, priceNormal = 6.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await

    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceNormal gt ItemMapping.priceSale)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceNormal gte ItemMapping.priceSale)).await.size === 3)

    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceSale lt ItemMapping.priceNormal)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceSale lte ItemMapping.priceNormal)).await.size === 3)
    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceNormal equ ItemMapping.priceSale)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "321312 - Sample item") and (ItemMapping.priceNormal neq ItemMapping.priceSale)).await.size === 2)

  }

  it should "search with geo_distance" in {
    val item1 = Item(name = "211 - Sample item", priceSale = 0.5, priceNormal = 1.0, validUntil = now, location = GeoPoint(10, 10), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "211 - Sample item", priceSale = 3.0, priceNormal = 4.0, validUntil = now, location = GeoPoint(11, 11), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "211 - Sample item", priceSale = 6.0, priceNormal = 6.0, validUntil = now, location = GeoPoint(20, 20), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await

    assert(dao.search((ItemMapping.name equ "211 - Sample item") and (ItemMapping.location geo_distance (GeoPoint(10, 10), 1000))).await.size === 2)
    assert(dao.search((ItemMapping.name equ "211 - Sample item") and (ItemMapping.location geo_distance (GeoPoint(20, 20), 1000))).await.size === 1)

  }

  it should "search with missing" in {
    val item1 = Item(name = "311 - Sample item", expectedProfit = Some(10), priceSale = 0.5, priceNormal = 1.0, validUntil = now, location = GeoPoint(10, 10), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "311 - Sample item", priceSale = 3.0, priceNormal = 4.0, validUntil = now, location = GeoPoint(11, 11), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "311 - Sample item", priceSale = 6.0, priceNormal = 6.0, validUntil = now, location = GeoPoint(20, 20), properties = Property(name = "Property3", likes = Some(11)) :: Property(name = "Property4", likes = Some(10)) :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await

    assert(dao.search((ItemMapping.name equ "311 - Sample item") and (ItemMapping.expectedProfit missing)).await.size === 2)
    assert(dao.search((ItemMapping.name equ "311 - Sample item") and (ItemMapping.properties / PropertyMapping.likes missing)).await.size === 2)

  }
  
  it should "search with exists" in {
    val item1 = Item(name = "411 - Sample item", expectedProfit = Some(10), priceSale = 0.5, priceNormal = 1.0, validUntil = now, location = GeoPoint(10, 10), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item2 = Item(name = "411 - Sample item", priceSale = 3.0, priceNormal = 4.0, validUntil = now, location = GeoPoint(11, 11), properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    val item3 = Item(name = "411 - Sample item", priceSale = 6.0, priceNormal = 6.0, validUntil = now, location = GeoPoint(20, 20), properties = Property(name = "Property3", likes = Some(11)) :: Property(name = "Property4", likes = Some(10)) :: Nil)

    dao.create(item1, true).await
    dao.create(item2, true).await
    dao.create(item3, true).await

    assert(dao.search((ItemMapping.name equ "411 - Sample item") and (ItemMapping.expectedProfit exists)).await.size === 1)
    assert(dao.search((ItemMapping.name equ "411 - Sample item") and (ItemMapping.properties / PropertyMapping.likes exists)).await.size === 1)

  }
  
}