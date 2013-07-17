package com.busymachines.commons.test
import org.scalatest.FlatSpec
import com.busymachines.commons.test.TestAssembly.itemDao
import com.busymachines.commons.implicits._
import com.busymachines.commons.dao.elasticsearch.implicits._

class ItemDaoTests extends FlatSpec with EmptyAurumElasticSearchStorage {
  "ItemDao" should "create & retrieve" in {
    val item = Item(name = "Sample item", properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    itemDao.create(item, true).await
    assert(itemDao.retrieve(item.id).await.get.id === item.id)
  }
}