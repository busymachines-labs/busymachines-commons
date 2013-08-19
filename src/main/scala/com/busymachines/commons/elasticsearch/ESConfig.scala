package com.busymachines.commons.elasticsearch

import com.busymachines.commons.implicits._
import com.busymachines.commons.CommonConfig

class ESConfig(baseName: String) extends CommonConfig(baseName) {
  def clusterName = config.getStringOption("clusterName") getOrElse "elasticsearch"
  def hostNames = config.getStringSeq("hostNames") nonEmptyOrElse Seq("localhost")
  def numberOfShards = config.getIntOption("numberOfShards") getOrElse 1
  def numberOfReplicas = config.getIntOption("numberOfReplicas") getOrElse 0
  def port = config.getIntOption("port") getOrElse 9300
}