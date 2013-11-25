package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.Logging
import org.joda.time.DateTime
import java.net.Inet4Address
import com.busymachines.commons.domain.GeoPoint
import spray.json.JsonWriter

object ESProperty {
  implicit def toPath[A, T](property: ESProperty[A, T]) = Path[A, T](property :: Nil)
}

case class ESProperty[A, T](name: String, mappedName: String, options: ESMapping.Options[T]) extends PathElement[A, T] {
  val nestedProperties = options.options.find(_.name == "properties").map(_.value.asInstanceOf[ESMapping.Properties[A]])
  def fieldName = mappedName
  // Needed to prevent other implicit === methods

  def geo_distance(geoPoint: GeoPoint, distanceInKm: Double) = ESSearchCriteria.GeoDistance(this, geoPoint, distanceInKm)

  def equ(path: Path[A, T]) = ESSearchCriteria.FEq(this, path)
  def equ(property: ESProperty[A, T]) = ESSearchCriteria.FEq(this, property)
  def equ[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Eq(this, value)

  def neq(path: Path[A, T]) = ESSearchCriteria.FNeq(this, path)
  def neq(property: ESProperty[A, T]) = ESSearchCriteria.FNeq(this, property)
  def neq[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Neq(this, value)

  def gt(path: Path[A, T]) = ESSearchCriteria.FGt(this, path)
  def gt(property: ESProperty[A, T]) = ESSearchCriteria.FGt(this, property)
  def gt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Gt(this, value)

  def gte(path: Path[A, T]) = ESSearchCriteria.FGte(this, path)
  def gte(property: ESProperty[A, T]) = ESSearchCriteria.FGte(this, property)
  def gte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Gte(this, value)

  def lt(path: Path[A, T]) = ESSearchCriteria.FLt(this, path)
  def lt(property: ESProperty[A, T]) = ESSearchCriteria.FLt(this, property)
  def lt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Lt(this, value)

  def lte(path: Path[A, T]) = ESSearchCriteria.FLte(this, path)
  def lte(property: ESProperty[A, T]) = ESSearchCriteria.FLte(this, property)
  def lte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Lte(this, value)

  def in[V](values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.In(this, values)
  def missing = ESSearchCriteria.missing(this)
  def exists = ESSearchCriteria.exists(this)
}

trait PathElement[A, T] {
  def fieldName : String 
}
object PathElement {
  def apply[A, T](s : String) = new PathElement[A, T] {
    def fieldName = s
  }
}

case class Path[A, T](elements: List[PathElement[_, _]]) {
  def head: PathElement[A, _] = elements.head.asInstanceOf[PathElement[A, _]]
  def last: PathElement[_, T] = elements.head.asInstanceOf[PathElement[_, T]]
  def /[A2 <: T, V2](property: PathElement[A2, V2]) = Path[A, V2](elements :+ property)
  def /[A2 <: T, V2](fieldName : String) = Path[A, V2](elements :+ PathElement[A, V2](fieldName))

  def geo_distance(geoPoint: GeoPoint, distanceInKm: Double) = ESSearchCriteria.GeoDistance(this, geoPoint, distanceInKm)

  def equ(path: Path[A, T]) = ESSearchCriteria.FEq(this, path)
  def equ[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Eq(this, value)
  def neq(path: Path[A, T]) = ESSearchCriteria.FNeq(this, path)
  def neq[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Neq(this, value)
  def gt(path: Path[A, T]) = ESSearchCriteria.FGt(this, path)
  def gt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Gt(this, value)
  def gte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Gte(this, value)
  def lt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Lt(this, value)
  def lte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.Lte(this, value)
  def in[V](values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = ESSearchCriteria.In(this, values)
  def missing = ESSearchCriteria.missing(this)
  def exists = ESSearchCriteria.exists(this)
  def nested(criteria : ESSearchCriteria[T]) = ESSearchCriteria.Nested(this)(criteria)
  def ++[V] (other : Path[T, V]) = Path[A, V](elements ++ other.elements)

  def toESPath =
    elements.map(_.fieldName).mkString(".")
}

object ESMapping {

  case class Properties[A](properties: List[ESProperty[A, _]]) {
    lazy val propertiesByName = properties.groupBy(_.name).mapValues(_.head)
    lazy val propertiesByMappedName = properties.groupBy(_.mappedName).mapValues(_.head)
  }

  case class Option(name: String, value: Any)
  case class Options[T](options: Option*) {
    options.groupBy(_.name).collect {
      case (name, values) if values.size > 1 =>
        throw new Exception("Incompatible options: " + values.mkString(", "))
    }
    def &(option: Option) = Options[T]((options.toSeq ++ Seq(option)): _*)
  }
}

class ESMapping[A] extends Logging {

  import ESMapping._

  protected var _allProperties: Properties[A] = Properties(Nil)

  def allProperties = _allProperties

  def mappingConfiguration(doctype: String): String =
    print(mapping(doctype), "\n")

  def mapping(doctype: String) = Properties[Any](List(ESProperty[Any, A](doctype, doctype, Options[A]((Seq(
    Option("_all", Map("enabled" -> true)),
    Option("_source", Map("enabled" -> true)),
    Stored,
    Option("properties", allProperties))): _*))))

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
  val IncludeInAll = Option("include_in_all", "true")
  def Nested[T](properties: Properties[T]): Options[T] = Options(Option("type", "nested"), Option("properties", properties))
  def Nested[T](mapping: ESMapping[T]): Options[T] = Nested(mapping._allProperties)
  val _all = ESProperty("_all", "_all", String)

  implicit class RichName(name: String) extends RichMappedName(name, name)

  implicit class RichMappedName(name: (String, String)) {
    def as[T](options: Options[T]) = add(ESProperty[A, T](name._1, name._2, options))
    def add[T](property: ESProperty[A, T]) = {
      _allProperties = Properties(_allProperties.properties :+ property)
      property
    }
    implicit def toProperty = ESProperty(name._1, name._2, Options())
  }
  def print(obj: Any, indent: String): String = obj match {
    case properties: Properties[A] =>
      properties.properties.map(p => "\"" + p.mappedName + "\": " + print(p.options, indent + "    ")).mkString(indent + "{" + indent + "  ", "," + indent + "  ", indent + "}")
    case options: Options[_] =>
      options.options.map(print(_, indent)).mkString("{", ", ", "}")
    case option: Option =>
      "\"" + option.name + "\": " + print(option.value, indent)
    case s: String => "\"" + s.toString + "\""
    case json: Map[_, _] => json.map(t => "\"" + t._1 + "\": " + print(t._2, indent)).mkString("{", ", ", "}")
    case v => v.toString
  }
}
