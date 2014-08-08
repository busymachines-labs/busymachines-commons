package com.busymachines.commons.testing

import scala.collection.concurrent

import com.busymachines.commons.elasticsearch.{ ESConfig, ESIndex }
import com.busymachines.commons.event.{ DoNothingEventSystem, EventBus }

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.getOrElse(baseName, 0)
    usedIndexes(baseName) = i + 1
    sanitizeName(baseName + (if (i > 0) i else ""))
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

  //TODO: THIS IS A TERRIBLE HACK, this is why traits mixins are better to bring in test data than function parameters. 
  private def hackGetProjectBaseName(c: Class[_]) = {
    val name = c.getCanonicalName
    val projectName = if (name.contains("prefab")) "prefab-" else if (name.contains("commons")) "commons-" else if (name.contains("aurum")) "aurum-" else ""
    "test-" + projectName + c.getSimpleName
  }
}

class EmptyESTestIndex(c: Class[_], config: ESConfig = DefaultTestESConfig, eventBus: EventBus = DoNothingEventSystem)
  extends ESIndex(config, EmptyESTestIndex.getNextName(EmptyESTestIndex.hackGetProjectBaseName(c)), eventBus) {
  drop()
  client.javaClient.admin().cluster().prepareHealth().setWaitForActiveShards(1).execute().actionGet()
  client.javaClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet()
}
