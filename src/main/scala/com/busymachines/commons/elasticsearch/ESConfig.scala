package com.busymachines.commons.elasticsearch

import com.busymachines.commons.CommonConfig

class ESConfig(baseName: String) extends CommonConfig(baseName) {
  def clusterName = string("clusterName") 
  def indexName = string("index") // Optional, can also specified with ESIndex constructor
  def hostNames = stringSeq("hostNames")
  def numberOfShards = int("numberOfShards") 
  def numberOfReplicas = int("numberOfReplicas") 
  def port = int("port") 
}