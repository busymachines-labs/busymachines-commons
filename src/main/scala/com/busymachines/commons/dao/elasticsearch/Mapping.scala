package com.busymachines.commons.dao.elasticsearch

case class Property(name : String, mappedName : String, options : Mapping.Options) {
  def / (property : Property) = Path(Seq(this, property))
  val nestedProperties = options.options.find(_.name == "properties").map(_.value.asInstanceOf[Mapping.Properties])
  implicit def toPath = Path(Seq(this))
}

case class Path(properties : Seq[Property]) {
  def / (property : Property) = Path(properties :+ property)
}

object Mapping {

  case class Properties(properties : Seq[Property]) {
    lazy val propertiesByName = properties.groupBy(_.name).mapValues(_.head)
    lazy val propertiesByMappedName = properties.groupBy(_.mappedName).mapValues(_.head)
  }
  
  case class Option(name : String, value : Any) {
    def & (option : Option) = Options(Seq(this, option))
    def & (options : Options) = Options(Seq(this) ++ options.options)
  }

  case class Options(options : Seq[Option]) {
    def & (option : Option) = Options(options ++ Seq(option))
    def & (options : Options) = Options(this.options ++ options.options)
  }
}

class Mapping {

  import Mapping._
  
  protected var _allProperties : Properties = Properties(Seq.empty)
  
  def allProperties = _allProperties
  
  def mappingConfiguration(doctype : String): String = 
    print(mapping(doctype), "\n")
  
  def mapping(doctype : String) = Properties(Seq(Property(doctype, doctype,
    Option("_all", "{\"enabled\" : true}") & 
    Option("_source", "{\"enabled\" : true}") &
    Stored &
    Nested(_allProperties))))
    
  val String = Option("type", "string")
  val Integer = Option("type", "integer")
  val Boolean = Option("type", "boolean")
  val Nested = Option("type", "nested")
  val Stored = Option("store", "true")
  val Analyzed = Option("index", "\"analyzed\"")
  val NotAnalyzed = Option("index", "\"not_analyzed\"")
  def Nested(properties : Properties) : Options = Options(Seq(Option("type", "nested"), Option("properties", properties)))
  def Nested(mapping : Mapping) : Options = Nested(mapping._allProperties)
  
  implicit class RichName(name : String) extends RichMappedName(name, name)
  
  implicit class RichMappedName(name : (String, String)) {
    def as(option : Option) = add(Property(name._1, name._2, Options(Seq(option))))
        def as(options : Options) = add(Property(name._1, name._1, options))
        def add(property : Property) = {
          _allProperties = Properties(_allProperties.properties :+ property)
          property
    }
    implicit def toProperty = Property(name._1, name._2, Options(Seq.empty))
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
