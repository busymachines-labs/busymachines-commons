package com.busymachines.commons.test
import org.scalatest.FlatSpec
import com.busymachines.commons.test.TestAssembly.itemDao
import com.busymachines.commons.implicits._
import com.busymachines.commons.dao.elasticsearch.ESSearchCriteria._
import com.busymachines.commons.dao.elasticsearch.Path
import com.busymachines.commons.dao.elasticsearch.ESSearchCriteria
import com.busymachines.commons.dao.elasticsearch.implicits._

class ItemDaoTests extends FlatSpec with EmptyAurumElasticSearchStorage {
  "ItemDao" should "create & retrieve" in {
    val item = Item(name = "Sample item", properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    itemDao.create(item, true).await
    assert(itemDao.retrieve(item.id).await.get.id === item.id)
  }

  it should "create & update & retrieve" in {
    val item = Item(name = "Sample item 1", properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    itemDao.create(item, true).await
    assert(itemDao.retrieve(item.id).await.get.id === item.id)
    itemDao.modify(item.id)(_=>item.copy(name="Sample item 2")).await
    assert(itemDao.retrieve(item.id).await.get.name === "Sample item 2")    
  }
  
  it should "create & delete" in {
    val item = Item(name = "Sample item", properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    itemDao.create(item, true).await
    assert(itemDao.retrieve(item.id).await.get.id === item.id)
    itemDao.delete(item.id, true).await
    assert(itemDao.retrieve(item.id).await === None)
  }

  it should "create & search" in {
    val item = Item(name = "Sample item", properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
    itemDao.create(item, true).await
    assert(itemDao.search(new Delegate(ItemMapping.properties / PropertyMapping.name === "Property3")).await.size === 1)
  }

}