package com.busymachines.commons.test

import scala.concurrent.ExecutionContext.Implicits.global
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

class ItemDaoTests extends FlatSpec with Logging {

  val esIndex = new EmptyESTestIndex(getClass,new DoNothingEventSystem)
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
      case property if property.id == id => found(property); false
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

    dao.delete(item.id, true).await
    assert(dao.retrieve(item.id).await === None)
  }

  it should "create & search for simple nested object" in {
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.properties / PropertyMapping.name === "Property3").await.size === 1)
  }

  it should "search by id" in {
    val propertyId = Id.generate[Property]
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(id = propertyId,name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.id === item.id).await.size === 1)
    assert(dao.searchSingle(ItemMapping.properties / PropertyMapping.id === propertyId).await.get.name === item.name)
  }

  it should "search by id using in" in {
    val propertyId = Id.generate[Property]
    val item = Item(name = "Sample item", validUntil = now, location = geoPoint, properties = Property(id = propertyId,name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.id in item.id::Nil).await.size === 1)
    assert(dao.searchSingle(ItemMapping.properties / PropertyMapping.id in propertyId::Nil).await.get.name === item.name)
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

    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(0,1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(1,1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(2,1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(3,1)).await.size === 1)
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(4,1)).await.size === 1)

     assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(0,3)).await.size === 3)
     assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page(0,5)).await.size === 5)
   
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
    
    
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page.all,ESSearchSort.asc("name")).await.result(0).name === "A Sample item")
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page.all,ESSearchSort.asc("name")).await.result(1).name === "B Sample item")
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page.all,ESSearchSort.asc("name")).await.result(2).name === "C Sample item")
    assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page.all,ESSearchSort.asc("name")).await.result(3).name === "D Sample item")
    
    //assert(dao.search(ItemMapping.id in item1.id::item2.id::item3.id::item4.id::item5.id::Nil,Page.all).await === dao.search(ItemMapping.id in item2.id::item1.id::item3.id::item4.id::item5.id::Nil,Page.all).await)
    }
  
}