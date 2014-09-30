package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.elasticsearch.ESSearchCriteria.And
import com.busymachines.commons.elasticsearch.Mappings.ItemMapping
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.testing.EmptyESTestIndex
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.busymachines.commons.Implicits.richFuture
import com.busymachines.commons.elasticsearch.Mappings._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by alex on 30.09.2014.
 */
class CountApiTests extends FlatSpec with Logging{
 //TODO Removed since count API is not properly supported because it is based on query not on filters

 /* val esIndex = EmptyESTestIndex(getClass)
  val dao = new ESRootDao[Item](esIndex, ESType("item", ItemMapping))

  val geoPoint = GeoPoint(10, 10)

  "Count API" should "return 0 when no items found" in {
    assert(dao.count(ESSearchCriteria.All[Item]).await == 0)
  }

  it should "work with all search criteria when 1 document" in {
    val item = Item(name = "Sample item", `type` = Some(ItemType.Bicycle), validUntil = DateTime.now, location = geoPoint, properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil)
    val res = dao.create(item, true).await
    assert(dao.count(ESSearchCriteria.All[Item]).await == 1)
    dao.delete(res.id).await
  }

  it should "work with all search criteria when 10 documents" in {
    val item =Future.sequence((1 to 10).map(r=>dao.create(
        Item(name = "Sample item",
          `type` = Some(ItemType.Bicycle),
          validUntil = DateTime.now,
          location = geoPoint,
          properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil),true))).await

    assert(dao.count(ESSearchCriteria.All[Item]).await ==10)
  }

  it should "work with particular search criteria" in {
      val item =Future.sequence((1 to 10).map(r=>dao.create(
        Item(name = s"Sample item ${r%3}",
          `type` = Some(ItemType.Car),
          validUntil = DateTime.now,
          location = geoPoint,
          properties = Property(name = "Property1") :: Property(name = "Property2") :: Nil),true))).await

      assert(dao.count(And((ItemMapping.`type` equ "car") :: (ItemMapping.name equ "Sample item 2") :: Nil)).await == 3)
  }*/

}
