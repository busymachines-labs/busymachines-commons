package com.busymachines.commons.dao.elasticsearch

import com.busymachines.commons.domain.HasId

object Property {
  implicit def toPath[A, T](property : Property[A, T]) = Path[A, T](property :: Nil)
}

case class Property[A, T](name : String, mappedName : String, options : Mapping.Options[T]) {
  val nestedProperties = options.options.find(_.name == "properties").map(_.value.asInstanceOf[Mapping.Properties[A]])
}

case class Path[A, T](properties : List[Property[_, _]]) {
  def head : Property[A, _] = properties.head.asInstanceOf[Property[A, _]]
  def last : Property[_, T] = properties.head.asInstanceOf[Property[_, T]]
  def / [A2 <: T, V2](property : Property[A2, V2]) = Path[A, V2](properties :+ property)
  def === (value : T) = ESSearchCriteria.Equals(this, value)
}

object Mapping {

  case class Properties[A](properties : List[Property[A, _]]) {
    lazy val propertiesByName = properties.groupBy(_.name).mapValues(_.head)
    lazy val propertiesByMappedName = properties.groupBy(_.mappedName).mapValues(_.head)
  }
  
  case class Option(name : String, value : Any) 
  case class Options[T](options : Option*) {
    def & (option : Option) = Options[T]((options.toSeq ++ Seq(option)):_*)
  }
}

class Mapping[A] {

  import Mapping._
  
  protected var _allProperties : Properties[A] = Properties(Nil)
  
  def allProperties = _allProperties
  
  def mappingConfiguration(doctype : String): String = 
    print(mapping(doctype), "\n")
  
  def mapping(doctype : String) = Properties[Any](List(Property[Any, A](doctype, doctype, Options[A](
    (Seq(Option("_all", "{\"enabled\" : true}"),
    Option("_source", "{\"enabled\" : true}"),
    Stored) ++
    Nested[A](allProperties).options):_*))))
    
  val String = Options[String](Option("type", "string"))
  val Integer = Options[Int](Option("type", "integer"))
  val Boolean = Options[Boolean](Option("type", "boolean"))
  val Nested = Option("type", "nested")
  val Stored = Option("store", "true")
  val Analyzed = Option("index", "\"analyzed\"")
  val NotAnalyzed = Option("index", "\"not_analyzed\"")
  def Nested[T](properties : Properties[T]) : Options[T] = Options(Option("type", "nested"), Option("properties", properties))
  def Nested[T](mapping : Mapping[T]) : Options[T] = Nested(mapping._allProperties)
  
  implicit class RichName(name : String) extends RichMappedName(name, name)
  
  implicit class RichMappedName(name : (String, String)) {
    def as[T](options : Options[T]) = add(Property[A, T](name._1, name._1, options))
    def add[T](property : Property[A, T]) = {
      _allProperties = Properties(_allProperties.properties :+ property)
      property
    }
    implicit def toProperty = Property(name._1, name._2, Options())
  }

  def print(obj : Any, indent : String) : String = obj match {
    case properties : Properties[A] => 
      properties.properties.map(p => "\"" + p.name + "\": " + print(p.options, indent + "    ")).mkString(indent + "{" + indent + "  ", "," + indent + "  ", indent + "}")
    case options : Options[_] =>
      options.options.map(print(_, indent)).mkString("{", ", ", "}")
    case option : Option =>
      "\"" + option.name + "\": " + print(option.value, indent)
  }
}
