package com.busymachines.commons.test

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.FlatSpec
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.elasticsearch.ESSearchCriteria.Delegate
import com.busymachines.commons.elasticsearch.EsRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.implicits.richFuture
import com.busymachines.commons.test.DomainJsonFormats.itemFormat
import com.busymachines.commons.testing.EmptyESTestIndex
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class ItemDaoTests extends FlatSpec with EmptyESTestIndex {

  val dao = new EsRootDao[Item](esIndex, ESType("item", ItemMapping))

  "ItemDao" should "create & retrieve" in {
    val now = DateTime.now(DateTimeZone.UTC)
    val item = Item(name = "Sample item", validUntil = now, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    assert(dao.retrieve(item.id).await.get.validUntil === now)
  }

  it should "create & update & retrieve" in {
    val now = DateTime.now(DateTimeZone.UTC)
    val item = Item(name = "Sample item 1", validUntil = now, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    dao.modify(item.id)(_ => item.copy(name = "Sample item 2")).await
    assert(dao.retrieve(item.id).await.get.name === "Sample item 2")
    assert(dao.retrieve(item.id).await.get.validUntil === now)
  }

  it should "create & delete" in {
    val now = DateTime.now(DateTimeZone.UTC)
    val item = Item(name = "Sample item", validUntil = now, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    dao.create(item, true).await
    assert(dao.retrieve(item.id).await.get.id === item.id)
    assert(dao.retrieve(item.id).await.get.validUntil === now)
    dao.delete(item.id, true).await
    assert(dao.retrieve(item.id).await === None)
  }

  it should "create & search for simple nested object" in {
    val now = DateTime.now(DateTimeZone.UTC)
    val item = Item(name = "Sample item", validUntil = now, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    dao.create(item, true).await
    assert(dao.search(ItemMapping.properties / PropertyMapping.name === "Property3").await.size === 1)

  }

}