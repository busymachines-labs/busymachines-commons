package com.busymachines.commons.testing

import com.busymachines.commons.elasticsearch.ESConfig

/**
 * Created by lorand on 25.06.2014.
 */
object DefaultTestESConfig extends ESConfig("") {
  override def clusterName: String = "elasticsearch"

  override def indexName: String = "test"

  override def hostNames: Seq[String] = Seq("localhost")

  override def numberOfShards: Int = 1

  override def numberOfReplicas: Int = 0

  override def port: Int = 9300
}
