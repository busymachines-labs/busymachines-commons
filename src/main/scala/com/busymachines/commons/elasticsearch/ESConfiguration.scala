package com.busymachines.commons.elasticsearch

import com.busymachines.commons.implicits._
import com.busymachines.commons.HasConfiguration

class ESConfiguration extends HasConfiguration {
  def clusterName = config.getStringOption("clusterName") getOrElse "elasticsearch"
  def indexName = config.getString("indexName") 
  def hostNames = config.getStringSeq("hostNames") nonEmptyOrElse Seq("localhost")
  def numberOfShards = config.getIntOption("numberOfShards") getOrElse 1
  def numberOfReplicas = config.getIntOption("numberOfReplicas") getOrElse 0
  def port = config.getIntOption("port") getOrElse 9300
}