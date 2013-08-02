package com.busymachines.commons.testing

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESConfiguration
import collection.mutable
import org.scalatest.BeforeAndAfterAll

object EmptyESTestIndex {
  private val usedIndexes = mutable.Map[String, Int]()
  def getNextName(baseName : String) : String = {
    val i = usedIndexes.get(baseName).getOrElse(0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
}

trait EmptyESTestIndex extends BeforeAndAfterEach {
  
  // This trait can only be used on a test class.
  suite: Suite =>
  
  val esConfig = new ESConfiguration
  val esClient = new ESClient(esConfig)
  val esIndexBaseName = getClass.getName.toLowerCase
  val esIndex = new ESIndex(esClient, EmptyESTestIndex.getNextName(esIndexBaseName))
  esIndex.drop

  override protected def beforeEach {
//    esIndex.drop
//    Thread.sleep(1000)
//    esIndex.initialize
  }
}