package com.busymachines.commons.testing

import scala.collection.concurrent

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.node.NodeBuilder

import com.busymachines.commons.elasticsearch.{ ESConfig, ESIndex }
import com.busymachines.commons.event.{ DoNothingEventSystem, EventBus }

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.getOrElse(baseName, 0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
  lazy val node = NodeBuilder.nodeBuilder().local(true).data(true).node();
}

class EmptyESTestIndex(c: Class[_], config: ESConfig = DefaultTestESConfig, eventBus: EventBus = DoNothingEventSystem)
  extends ESIndex(config, EmptyESTestIndex.getNextName("test-" + c.getSimpleName.toLowerCase), eventBus) {

  try {

    EmptyESTestIndex.node.client().admin().cluster().health(new ClusterHealthRequest(indexName).waitForActiveShards(1)).actionGet();
    //    client.javaClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    //    val deleteQuery = org.elasticsearch.client.Requests.delete("_all")
    //    client.javaClient.admin().indices().delete(deleteQuery)
    drop()
  } catch {
    case e: Throwable => {
      debug(s"Failed to drop index: '${indexName}'' due to:${e.getMessage}: \n. Retrying.")
      println(s"\nFailed to drop index: '${indexName}'' due to:${e.getMessage}: \n. Retrying.\n")
      Thread.sleep(2000)
      try {
        drop()
      } catch {
        case e: Throwable => {
          debug(s"Failed to drop index a second time: '${indexName}'' due to:${e.getMessage}: \n. Retrying for the last time.")
          debug(s"\nFailed to drop index a second time: '${indexName}'' due to:${e.getMessage}: \n. Retrying for the last time.\n")
          Thread.sleep(2000)
          drop()
        }
      }
    }
  }
}
