package com.busymachines.commons.testing

import scala.collection.concurrent

import com.busymachines.commons.elasticsearch.{ ESConfig, ESIndex }
import com.busymachines.commons.event.{ DoNothingEventSystem, EventBus }

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.getOrElse(baseName, 0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
}

class EmptyESTestIndex(c: Class[_], config: ESConfig = DefaultTestESConfig, eventBus: EventBus = DoNothingEventSystem)
  extends ESIndex(config, EmptyESTestIndex.getNextName("test-" + c.getSimpleName.toLowerCase), eventBus) {

  drop()
  Thread.sleep(1000)
}
