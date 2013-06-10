package com.busymachines.commons.elasticsearch

case class Path(properties : Seq[Mapping.Property])

object Mapping {
  case class Option(name : String, value : Any) {
    def & (option : Option) = Options(Seq(this, option))
    def & (options : Options) = Options(Seq(this) ++ options.options)
  }

  case class Property(name : String, options : Options) {
    def / (property : Property) = Path(Seq(this, property))
    implicit def toPath = Path(Seq(this))
  }

  case class Properties(properties : Seq[Property])
  
  case class Options(options : Seq[Option]) {
    def & (option : Option) = Options(options ++ Seq(option))
    def & (options : Options) = Options(this.options ++ options.options)
  }
}

class Mapping {

  import Mapping._
  
  protected var properties : Properties = Properties(Seq.empty)
  
  def mappingConfiguration(doctype : String): String = 
    print(mapping(doctype), "\n")
  
  def mapping(doctype : String) = Properties(Seq(Property(doctype, 
    Option("_all", "{\"enabled\" : true}") & 
    Option("_source", "{\"enabled\" : true}") &
    Stored &
    Nested(properties))))
    
  val String = Option("type", "string")
  val Integer = Option("type", "integer")
  val Boolean = Option("type", "boolean")
  val Nested = Option("type", "nested")
  val Stored = Option("store", "true")
  val Analyzed = Option("index", "\"analyzed\"")
  val NotAnalyzed = Option("index", "\"not_analyzed\"")
  def Nested(properties : Properties) : Options = Options(Seq(Option("type", "nested"), Option("properties", properties)))
  def Nested(mapping : Mapping) : Options = Nested(mapping.properties)
  
  implicit class RichName(name : String) {
    def as(option : Option) = add(Property(name, Options(Seq(option))))
    def as(options : Options) = add(Property(name, options))
    def add(property : Property) = {
      properties = Properties(properties.properties :+ property)
      property
    }
    implicit def toProperty = Property(name, Options(Seq.empty))
  }

  def print(obj : Any, indent : String) : String = obj match {
    case properties : Properties => 
      properties.properties.map(p => "\"" + p.name + "\": " + print(p.options, indent + "    ")).mkString(indent + "{" + indent + "  ", "," + indent + "  ", indent + "}")
    case options : Options =>
      options.options.map(print(_, indent)).mkString("{", ", ", "}")
    case option : Option =>
      "\"" + option.name + "\": " + print(option.value, indent)
  }
}
