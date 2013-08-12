package com.busymachines.commons.testing

import scala.collection.concurrent

import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfiguration
import com.busymachines.commons.elasticsearch.ESIndex

object EmptyESTestIndex {
  private val usedIndexes = concurrent.TrieMap[String, Int]()
  def getNextName(baseName: String): String = {
    val i = usedIndexes.get(baseName).getOrElse(0)
    usedIndexes(baseName) = i + 1
    baseName + (if (i > 0) i else "")
  }
}

class EmptyESTestIndex(name : String) extends ESIndex(
    new ESClient(new ESConfiguration), EmptyESTestIndex.getNextName("test-" + name)) {
  
  def this(c : Class[_]) = this(c.getName.toLowerCase)

  drop
  initialize
}
