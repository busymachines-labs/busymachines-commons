package com.busymachines.commons.testing

import scala.collection.concurrent

import com.busymachines.commons.elasticsearch.{ ESConfig, ESIndex }
import com.busymachines.commons.event.{ DoNothingEventSystem, EventBus }

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()

  private def getNextName(baseName: String): String = {
    //we start at 100 so that we have proper ordering for test cases. 
    //For instance TestSuite100 - is the first test in the suite. TestSuite101
    val i = usedIndexes.getOrElse(baseName, 100)
    usedIndexes(baseName) = i + 1
    sanitizeName(baseName + i)
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
      .replace("'", ".")
      .replace("`", ".")
      .replace("~", ".")
      .replace("-", ".")
      .replace("..", ".")
      .replace("...", ".").takeRight(255)

  private def dropAndWaitForESServerRecovery(index: EmptyESTestIndex): EmptyESTestIndex = {
    index.drop()
    index.client.javaClient.admin().cluster().prepareHealth().setWaitForActiveShards(1).execute().actionGet()
    index.client.javaClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet()
    index
  }

  def apply(c: Class[_], config: ESConfig = DefaultTestESConfig, eventBus: EventBus = DoNothingEventSystem) = {
    val index = new EmptyESTestIndex(getNextName(s"test.${c.getCanonicalName}"), config, eventBus)
    dropAndWaitForESServerRecovery(index)
  }

  /**
   * In case you want to impose your own naming scheme.
   */
  def apply(indexName: String, config: ESConfig, eventBus: EventBus) = {
    val index = new EmptyESTestIndex(sanitizeName(indexName), config, eventBus)
    dropAndWaitForESServerRecovery(index)
  }
}

class EmptyESTestIndex private (indexName: String, config: ESConfig, eventBus: EventBus)
  extends ESIndex(config, indexName, eventBus)
