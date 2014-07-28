package com.busymachines.commons.elasticsearch

import scala.collection.mutable.ListBuffer

case class ESType[A] (
  name : String,
  mapping : ESMapping[A]
) 

class ESTypes extends (String => Option[ESMapping[_]]) {
  private val types = new ListBuffer[ESType[_]]
  def all: Seq[ESType[_]] = types.toSeq
  def apply(s: String) = types.find(_.name == s).map(_.mapping)
  def ++ (types: ESTypes) = {
    val result = new ESTypes
    result.types ++= this.types
    result.types ++= types.types
    result
  } 
  def esType[A](name: String, mapping: ESMapping[A]): ESType[A] = {
    val t = ESType(name, mapping)
    types += t
    t
  }
}