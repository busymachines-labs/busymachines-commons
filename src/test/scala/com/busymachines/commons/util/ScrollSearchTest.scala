

package com.busymachines.commons.util

import akka.actor.ActorSystem
import com.busymachines.commons.Logging
import com.busymachines.commons.elasticsearch.{ESMapping, ESSearchCriteria, ESCollection}
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.testing.{DefaultTestESConfig, EmptyESTestIndex}
import com.busymachines.prefab.party.PartyAssembly
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import com.busymachines.prefab.party.logic.PartyFixture
import com.busymachines.commons.domain.{HasId, Id}
import com.busymachines.commons.Implicits._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt
import scala.util.Random

/**
 * Created by alex on 25.06.2014.
 */

object formats {
  implicit val someDocFormat = format2 (SomeDocument)
}

import formats._

case class SomeDocument (id: Id[SomeDocument], value: String) extends HasId[SomeDocument]

object SomeDocumentMapping extends ESMapping[SomeDocument] {
  implicit val someDocumentFormat = format2 (SomeDocument)
  val id = "_id" -> "id" :: String.as[Id[SomeDocument]]
  val value = "value" :: String
}

@RunWith (classOf[JUnitRunner])
class ScrollSearchTest extends FlatSpec with PartyAssembly with PartyFixture with Logging {

  lazy implicit val actorSystem: ActorSystem = ActorSystem("Commons",ConfigFactory.load("tests.conf"))
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val eventBus = new LocalEventBus(actorSystem)
  lazy val index = EmptyESTestIndex(getClass, DefaultTestESConfig, eventBus)

  val totalDocs: Int = 1001
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

  it should "retrieve first 10 documents" in {
    val it = collection.scan (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize)
    val col = ArrayBuffer[SomeDocument]()
    for (el <- it.take (10))
      col += el
    assert (col.size === 10)
  }

  it should "retrieve sequentially first 100 documents" in {
    val it = collection.scan (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize)
    val col = ArrayBuffer[SomeDocument]()
    for (el <- it.take (100))
      col += el
    assert (col.size === 100)
    assert (col.distinct.size === 100)
  }

  it should "retrieve sequentially first 1000 documents" in {
    val it = collection.scan (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize)
    val col = ArrayBuffer[SomeDocument]()
    for (el <- it.take (1000))
      col += el
    assert (col.size === 1000)
    assert (col.distinct.size === 1000)
  }

  it should "retrieve sequentially all documents" in {
    val it = collection.scan (ESSearchCriteria.All[SomeDocument], 5 minutes, batchSize)
    val col = ArrayBuffer[SomeDocument]()
    for (el <- it)
      col += el
    assert (col.size === totalDocs)


  }


}
