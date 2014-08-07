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

  private def sanitizeName(name: String) =
    name.toLowerCase.trim
      .replace("/", ".")
      .replace("\\", ".")
      .replace("*", ".")
      .replace("?", ".")
      .replace("\"", ".")
      .replace("<", ".")
      .replace(">", ".")
      .replace("|", ".")
      .replace(",", ".")
      .replace("\t", ".")
      .replace(" ", ".")
      .replace("_", ".")
      .replace("-", ".")
      .replace("..", ".")
      .replace("...", ".").takeRight(255)
}

class EmptyESTestIndex(c: Class[_], config: ESConfig = DefaultTestESConfig, eventBus: EventBus = DoNothingEventSystem)
  extends ESIndex(config, EmptyESTestIndex.sanitizeName(EmptyESTestIndex.getNextName("test-" + c.getSimpleName)), eventBus) {
  drop()
  Thread.sleep(1000)
}
