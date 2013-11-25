package com.busymachines.commons.testing

import scala.collection.concurrent
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfig
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.event.EventBus
import com.busymachines.commons.event.BusEvent
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.event.DoNothingEventSystem

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.get(baseName).getOrElse(0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
  lazy val doNothingEventBus = new DoNothingEventSystem
  lazy val client = new ESClient(new ESConfig("test.busymachines.db.elasticsearch")) }

class EmptyESTestIndex(client : ESClient, name : String , eventBus:EventBus) extends ESIndex(client, EmptyESTestIndex.getNextName("test-" + name),eventBus) {
  
  def this(c : Class[_],eventBus:EventBus,client:ESClient) = this(client, c.getName.toLowerCase,eventBus)
  def this(c : Class[_],eventBus:EventBus) = this(EmptyESTestIndex.client, c.getName.toLowerCase,eventBus)
  def this(c : Class[_]) = this(EmptyESTestIndex.client, c.getName.toLowerCase,EmptyESTestIndex.doNothingEventBus)

  drop
  initialize
}
