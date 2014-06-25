

package com.busymachines.commons.util

import com.busymachines.commons.Logging
import com.busymachines.commons.dao.Scroll
import com.busymachines.commons.elasticsearch.{ESMapping, ESSearchCriteria, ESCollection}
import com.busymachines.prefab.party.db.PartyMapping
import org.scalatest.FlatSpec
import com.busymachines.commons.test.AssemblyTestBase
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.party.logic.PartyFixture
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import spray.http.{StatusCodes, ContentTypes, HttpEntity}
import spray.json.JsonParser
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.{HasId, Id}
import com.busymachines.commons.Implicits._
import com.busymachines.prefab.party.Implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration.DurationInt
import com.busymachines.commons.spray._

import scala.util.Random

/**
 * Created by alex on 25.06.2014.
 */

object formats {
  implicit val someDocumentFormat = format2 (SomeDocument)
}

import formats._

case class SomeDocument (id: Id[SomeDocument], value: String) extends HasId[SomeDocument]

object SomeDocumentMapping extends ESMapping[SomeDocument] {
  val id = "_id" -> "id" :: String.as[Id[SomeDocument]]
  val value = "value" :: String
}

@RunWith (classOf[JUnitRunner])
class ScrollSearchTest extends FlatSpec with AssemblyTestBase with PartyFixture with Logging {

  val totalDocs: Int = 1010
  val batchSize: Int = 10

  val collection = new ESCollection[SomeDocument](index, "someDocument", SomeDocumentMapping)

  private def createDocFixture = {
    for (index <- 1 to totalDocs)
      collection.create (SomeDocument (Id.generate[SomeDocument], s"document $index"), true).await
  }

  "ScrollSearch" should s"create fixture by adding $totalDocs documents" in {
    createDocFixture
    assert (collection.retrieveAll.await.size == totalDocs)
  }

  it should "obtain a scroll" in {
    val scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    debug (s"Obtained scroll: $scroll")
    assert (scroll.duration === 5.minutes)
    assert (scroll.size === batchSize)
    assert (scroll.id != " ")
  }

  it should "retrieve first 10 documents" in {
    val scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll).await

    assert (result.result.size === batchSize)
    assert (result.totalCount === Some (totalDocs))
  }

  it should "retrieve sequentially first 100 documents" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()
    val expectedSize = 100

    while (count < expectedSize) {
      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll).await
      collectedResult = collectedResult ++ result.result.map (_.entity)
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
      count += batchSize
    }
    assert (collectedResult.size === expectedSize)
  }


  it should "retrieve sequentially first 1000 documents" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()
    val expectedSize: Int = 1000

    while (count < expectedSize) {
      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll).await
      collectedResult = collectedResult ++ result.result.map (_.entity)
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
      count += batchSize
    }
    assert (collectedResult.size === expectedSize)
    assert (collectedResult.distinct.size === expectedSize)
  }

  it should "retrieve sequentially all documents" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()

    do {
      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll).await
      collectedResult = collectedResult ++ result.result.map (_.entity)
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
      count = result.result.size
    } while (count != 0)

    assert (collectedResult.size === totalDocs)
    assert (collectedResult.distinct.size === totalDocs)
  }


  it should "retrieve even if scroll size changed" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()
    val expectedSize: Int = 100

    while (count < expectedSize) {
      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll.copy (size = Random.nextInt (100))).await
      collectedResult = collectedResult ++ result.result.map (_.entity)
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
      count += batchSize
      assert (result.result.size === batchSize)
    }

    assert (collectedResult.size === expectedSize)
    assert (collectedResult.distinct.size === expectedSize)
  }

  it should "retrieve even if scroll duration changed" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()
    val expectedSize: Int = 100

    while (count < expectedSize) {
      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], scroll.copy (duration = (Random.nextInt (10) + 1).seconds)).await
      collectedResult = collectedResult ++ result.result.map (_.entity)
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
      count += batchSize
      assert (result.result.size === batchSize)
    }

    assert (collectedResult.size === expectedSize)
    assert (collectedResult.distinct.size === expectedSize)
  }

  //TODO Check why appending a value to the given scrollId and executing another scroll search works :?
  it should " not retrieve if scroll id changed" in {
    var scroll = collection.prepareScroll (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize).await
    var count = 0
    var collectedResult = List[SomeDocument]()
    val expectedSize: Int = 100

    try {
      val newScroll = Scroll ("badId", scroll.duration, scroll.size)
      debug (s"Bad id used for request: ${newScroll.id}")

      val result = collection.scroll (ESSearchCriteria.All[SomeDocument], newScroll).await

      count = result.result.size
      scroll = result.scroll.getOrElse (throw new Exception ("Scroll not found in the result!"))
    } catch {
      case ex: Exception => debug (ex)
    }
    assert (count === 0)

  }

}
