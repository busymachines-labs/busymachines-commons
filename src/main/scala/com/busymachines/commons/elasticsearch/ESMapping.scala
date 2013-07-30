package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.Logging
import org.joda.time.DateTime
import java.net.Inet4Address
import com.busymachines.commons.domain.GeoPoint

object ESProperty {
  implicit def toPath[A, T](property : ESProperty[A, T]) = Path[A, T](property :: Nil)
}

case class ESProperty[A, T](name : String, mappedName : String, options : ESMapping.Options[T]) {
  val nestedProperties = options.options.find(_.name == "properties").map(_.value.asInstanceOf[ESMapping.Properties[A]])
}

case class Path[A, T](properties : List[ESProperty[_, _]]) {
  def head : ESProperty[A, _] = properties.head.asInstanceOf[ESProperty[A, _]]
  def last : ESProperty[_, T] = properties.head.asInstanceOf[ESProperty[_, T]]
  def / [A2 <: T, V2](property : ESProperty[A2, V2]) = Path[A, V2](properties :+ property)
  def === (value : T) = ESSearchCriteria.Equals(this, value)
}

object ESMapping {

  case class Properties[A](properties : List[ESProperty[A, _]]) {
    lazy val propertiesByName = properties.groupBy(_.name).mapValues(_.head)
    lazy val propertiesByMappedName = properties.groupBy(_.mappedName).mapValues(_.head)
  }
  
  case class Option(name : String, value : Any) 
  case class Options[T](options : Option*) {
    options.groupBy(_.name).collect {
      case (name, values) if values.size > 1 => 
        throw new Exception("Incompatible options: " + values.mkString(", "))
    }
    def & (option : Option) = Options[T]((options.toSeq ++ Seq(option)):_*)
  }
}

class ESMapping[A] extends Logging {

  import ESMapping._
  
  protected var _allProperties : Properties[A] = Properties(Nil)
  
  def allProperties = _allProperties
  
  def mappingConfiguration(doctype : String): String = 
    print(mapping(doctype), "\n")
  
  def mapping(doctype : String) = Properties[Any](List(ESProperty[Any, A](doctype, doctype, Options[A]((Seq(
      Option("_all", Map("enabled" -> true)),
      Option("_source", Map("enabled" -> true)),
      Stored, 
      Option("properties", allProperties))):_*))))
    
  val String = Options[String](Option("type", "string"))
  val Date = Options[DateTime](Option("type", "date"))
  def Object[T] = Options[T](Option("type", "object"))
  val Float = Options[Float](Option("type", "float"))
  val Double = Options[Double](Option("type", "double"))
  val Integer = Options[Int](Option("type", "integer"))
  val Long = Options[Long](Option("type", "long"))
  val Short = Options[Short](Option("type", "short"))
  val Byte = Options[Byte](Option("type", "byte"))
  val Boolean = Options[Boolean](Option("type", "boolean"))
  val Binary = Options[Array[Byte]](Option("type", "binary"))
  val Ipv4 = Options[Inet4Address](Option("type", "ip"))
  val GeoPoint = Options[GeoPoint](Option("type", "geo_point"))
  val Nested = Option("type", "nested")
  val Stored = Option("store", true)
  val NotIndexed = Option("index", "no")
  val Analyzed = Option("index", "analyzed")
  val NotAnalyzed = Option("index", "not_analyzed")
  def Nested[T](properties : Properties[T]) : Options[T] = Options(Option("type", "nested"), Option("properties", properties))
  def Nested[T](mapping : ESMapping[T]) : Options[T] = Nested(mapping._allProperties)
  
  implicit class RichName(name : String) extends RichMappedName(name, name)
  
  implicit class RichMappedName(name : (String, String)) {
    def as[T](options : Options[T]) = add(ESProperty[A, T](name._1, name._2, options))
    def add[T](property : ESProperty[A, T]) = {
      _allProperties = Properties(_allProperties.properties :+ property)
      property
    }
    implicit def toProperty = ESProperty(name._1, name._2, Options())
  }
  def print(obj : Any, indent : String) : String = obj match {
    case properties : Properties[A] => 
      properties.properties.map(p => "\"" + p.mappedName + "\": " + print(p.options, indent + "    ")).mkString(indent + "{" + indent + "  ", "," + indent + "  ", indent + "}")
    case options : Options[_] =>
      options.options.map(print(_, indent)).mkString("{", ", ", "}")
    case option : Option =>
      "\"" + option.name + "\": " + print(option.value, indent)
    case s : String => "\"" + s.toString + "\""
    case json : Map[_, _] => json.map(t => "\"" + t._1 + "\": " + print(t._2, indent)).mkString("{", ", ", "}")
    case v => v.toString
  }
}
