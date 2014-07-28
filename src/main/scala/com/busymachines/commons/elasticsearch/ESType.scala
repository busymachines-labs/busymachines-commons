package com.busymachines.commons.elasticsearch

import scala.collection.mutable.ListBuffer

case class ESType[A] (
  name : String,
  mapping : ESMapping[A]
) 

class ESTypes {
  private def types = new ListBuffer[ESType[_]]
  def all: Seq[ESType[_]] = types.toSeq
  def esType[A](name: String, mapping: ESMapping[A]): ESType[A] = {
    val t = ESType(name, mapping)
    types += t
    t
  }
}