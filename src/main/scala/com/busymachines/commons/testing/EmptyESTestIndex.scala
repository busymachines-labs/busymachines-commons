package com.busymachines.commons.testing

import scala.collection.concurrent
import com.busymachines.commons.elasticsearch.ESConfig
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.event.{DoNothingEventSystem, EventBus}

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.get(baseName).getOrElse(0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
}

class EmptyESTestIndex(config: ESConfig, name : String , eventBus:EventBus) extends ESIndex(config, EmptyESTestIndex.getNextName("test-" + name),eventBus) {
  
  def this(c : Class[_], eventBus: EventBus, config: ESConfig) = this(config, c.getName.toLowerCase,eventBus)
  def this(c : Class[_], eventBus: EventBus) = this(new ESConfig("test.busymachines.db.elasticsearch"), c.getName.toLowerCase,eventBus)
  def this(c : Class[_]) = this(new ESConfig("test.busymachines.db.elasticsearch"), c.getName.toLowerCase, DoNothingEventSystem)

  drop()
}
