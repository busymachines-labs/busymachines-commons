package com.busymachines.commons.test

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import org.scalatest.BeforeAndAfterEach

trait EmptyAurumElasticSearchStorage extends BeforeAndAfterEach {

  // This trait can only be used on a test class.
  suite: Suite =>

  override protected def beforeEach {
    super.beforeEach
    TestAssembly.index.drop
    TestAssembly.index.initialize
  }
  
}