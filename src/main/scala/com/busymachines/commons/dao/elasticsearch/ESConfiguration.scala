package com.busymachines.commons.dao.elasticsearch

import com.busymachines.commons.HasConfiguration
import scala.collection.JavaConversions._

class EsConfiguration extends HasConfiguration {
  val clusterName = config.getString("clusterName")
  val esHostNames:Seq[String] = config.getStringList("hostNames")
  val numberOfShards = if (config.hasPath("numberOfShards")) config.getInt("numberOfShards")  else 1
  val numberOfReplicas = if (config.hasPath("numberOfReplicas")) config.getInt("numberOfReplicas") else 0
  val esPort = config.getInt("port")
  val esIndexName = config.getString("indexName")
}