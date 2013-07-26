package com.busymachines.commons.testing

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import com.busymachines.commons.elasticsearch.ElasticSearchClient
import com.busymachines.commons.elasticsearch.Index
import com.busymachines.commons.elasticsearch.ElasticSearchConfiguration

trait EmptyESTestIndex extends BeforeAndAfterEach {
  
  // This trait can only be used on a test class.
  suite: Suite =>
  
  val esConfig = new ElasticSearchConfiguration
  val esClient = new ElasticSearchClient(esConfig)
  val esIndex = new Index(esClient, getClass.getName.toLowerCase)

  override protected def beforeEach {
    super.beforeEach
    esIndex.drop
    esIndex.initialize
  }
}