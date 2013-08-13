package com.busymachines.commons.testing

import scala.collection.concurrent
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfiguration
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.event.EventBus
import com.busymachines.commons.event.BusEvent
import com.busymachines.commons.event.LocalEventBus

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.get(baseName).getOrElse(0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
  lazy val client = new ESClient(new ESConfiguration) }

class EmptyESTestIndex(name : String) extends ESIndex(EmptyESTestIndex.client, EmptyESTestIndex.getNextName("test-" + name),null) {
  
  def this(c : Class[_]) = this(c.getName.toLowerCase)

  drop
  initialize
}
