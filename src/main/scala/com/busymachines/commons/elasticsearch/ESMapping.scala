package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.Logging
import org.joda.time.DateTime
import java.net.Inet4Address
import com.busymachines.commons.domain.GeoPoint
import spray.json.JsonWriter
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import com.busymachines.commons.domain.Money

object MoneyMapping extends ESMapping[Money] {
  val currency = "currency" as String & Analyzed
  val amount = "amount" as Double & Analyzed
}

object ESMapping extends ESMappingConstants {

  case class Properties[A](properties: List[ESProperty[A, _]]) {
    lazy val propertiesByName = properties.groupBy(_.name).mapValues(_.head)
    lazy val propertiesByMappedName = properties.groupBy(_.mappedName).mapValues(_.head)
  }

  case class PropertyOption(name: String, value: Any)
  case class Options[T](options: PropertyOption*) {
    options.groupBy(_.name).collect {
      case (name, values) if values.size > 1 =>
        throw new Exception("Incompatible options: " + values.mkString(", "))
    }
    def &(option: PropertyOption) = Options[T]((options.toSeq ++ Seq(option)): _*)
  }
}

trait ESMappingConstants {
  
  import ESMapping._
  
  val String = Options[String](PropertyOption("type", "string"))
  val Date = Options[DateTime](PropertyOption("type", "date"))
  def Object[T] = Options[T](PropertyOption("type", "object"))
  val Float = Options[Float](PropertyOption("type", "float"))
  val Double = Options[Double](PropertyOption("type", "double"))
  val Integer = Options[Int](PropertyOption("type", "integer"))
  val Long = Options[Long](PropertyOption("type", "long"))
  val Short = Options[Short](PropertyOption("type", "short"))
  val Byte = Options[Byte](PropertyOption("type", "byte"))
  val Boolean = Options[Boolean](PropertyOption("type", "boolean"))
  val Binary = Options[Array[Byte]](PropertyOption("type", "binary"))
  val Ipv4 = Options[Inet4Address](PropertyOption("type", "ip"))
  val GeoPoint = Options[GeoPoint](PropertyOption("type", "geo_point"))
  val Nested = PropertyOption("type", "nested")
  val Stored = PropertyOption("store", true)
  val NotIndexed = PropertyOption("index", "no")
  val Analyzed = PropertyOption("index", "analyzed")
  val NotAnalyzed = PropertyOption("index", "not_analyzed")
  val IncludeInAll = PropertyOption("include_in_all", "true")
  def Nested[T](mapping: ESMapping[T]): Options[T] = Options(Nested, PropertyOption("properties", mapping))
  val _all = new ESProperty("_all", "_all", String)
}

class ESMapping[A](implicit ct : ClassTag[A]) extends ESMappingConstants with Logging {

  import ESMapping._

  val caseClassFields : Map[String, CaseClassField] = CaseClassFields.of[A]
  
  private var _allPropertiesVar = Properties[A](Nil)

  def _allProperties = _allPropertiesVar.properties
  def _propertiesByName = _allPropertiesVar.propertiesByName
  def _propertiesByMappedName = _allPropertiesVar.propertiesByMappedName

  protected var ttl : Option[Duration] = None // enable ttl without a default ttl value: Some(Duration.Inf)
  
  def _mappingName = getClass.getName.stripSuffix("$")

  def mappingConfiguration(doctype: String): String =
    print(mapping(doctype), "\n")

  def mapping(doctype: String) =  {
    
    val errors = check
    if (errors.nonEmpty) {
      throw new Exception(s"Mapping errors found:\n  ${errors.mkString("\n  ")}")
    }
    
    Properties[Any](List(new ESProperty[Any, A](doctype, doctype, Options[A]((Seq(
      Some(PropertyOption("_all", Map("enabled" -> true))),
      Some(PropertyOption("_source", Map("enabled" -> true))),
      ttl map {
        case ttl if ttl.isFinite => PropertyOption("_ttl", Map("enabled" -> true, "default" -> ttl.toMillis))
        case ttl => PropertyOption("_ttl", Map("enabled" -> true))
      },
      Some(Stored),
      Some(PropertyOption("properties", this))).flatten: _*)))))
  }
  
  implicit class RichName(name: String) extends RichMappedName(name, name)

  implicit class RichMappedName(name: (String, String)) {
    def as[T](options: Options[T]) = add(new ESProperty[A, T](name._1, name._2, options))
    def add[T](property: ESProperty[A, T]) = {
      _allPropertiesVar = Properties(_allPropertiesVar.properties :+ property)
      property
    }
    implicit def toProperty = new ESProperty(name._1, name._2, Options())
  }
  
  private def check : Iterable[String] = {
    caseClassFields.keys.flatMap { fieldName =>
      if (_propertiesByName.contains(fieldName)) Seq.empty
      else Seq(s"Field '$fieldName' is not defined in mapping ${_mappingName}")
    } ++ _allProperties.flatMap(_.options.options).map(_.value).collect { case es : ESMapping[_] => es.check }.flatten
  }
  
  private def print(obj: Any, indent: String): String = obj match {
    case mapping: ESMapping[A] =>
      print(mapping._allPropertiesVar, indent)
    case properties: Properties[A] =>
      properties.properties.map(p => "\"" + p.mappedName + "\": " + print(p.options, indent + "    ")).mkString(indent + "{" + indent + "  ", "," + indent + "  ", indent + "}")
    case options: Options[_] =>
      options.options.map(print(_, indent)).mkString("{", ", ", "}")
    case option: PropertyOption =>
      "\"" + option.name + "\": " + print(option.value, indent)
    case s: String => "\"" + s.toString + "\""
    case json: Map[_, _] => json.map(t => "\"" + t._1 + "\": " + print(t._2, indent)).mkString("{", ", ", "}")
    case v => v.toString
  }
}

object ESProperty {
  implicit def toPath[A, T](property: ESProperty[A, T]) = Path[A, T](property :: Nil)
}

class ESProperty[A, T](val name: String, val mappedName: String, val options: ESMapping.Options[T]) 
extends PathElement[A, T](mappedName, options.options.contains(ESMapping.Nested)) {
  val nested = options.options.find(_.name == "nested").map(_.value.asInstanceOf[ESMapping[A]])

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

