package com.busymachines.commons.testing

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.Index
import com.busymachines.commons.elasticsearch.ESConfiguration

trait EmptyESTestIndex extends BeforeAndAfterEach {
  
  // This trait can only be used on a test class.
  suite: Suite =>
  
  val esConfig = new ESConfiguration
  val esClient = new ESClient(esConfig)
  val esIndex = new Index(esClient, getClass.getName.toLowerCase)

  override protected def beforeEach {
    super.beforeEach
    esIndex.drop
    esIndex.initialize
  }
}