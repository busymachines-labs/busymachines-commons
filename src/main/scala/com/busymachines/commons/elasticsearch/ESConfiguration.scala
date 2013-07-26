package com.busymachines.commons.elasticsearch

import com.busymachines.commons.implicits._
import com.busymachines.commons.HasConfiguration

class EsConfiguration extends HasConfiguration {
  val clusterName = config.getString("clusterName")
  val esHostNames = config.getStringSeq("hostNames")
  val numberOfShards = config.getIntOption("numberOfShards") getOrElse 1
  val numberOfReplicas = config.getIntOption("numberOfReplicas") getOrElse 0
  val esPort = config.getInt("port")
  val esIndexName = config.getString("indexName")
}