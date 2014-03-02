import spray.json.JsObject
import com.busymachines.commons.Extensions
import com.busymachines.commons.elasticsearch.CaseClassField
import com.busymachines.commons.elasticsearch.CaseClassFields
import spray.json.JsTrue
import scala.concurrent.duration.Duration
import spray.json.JsNumber

package com.busymachines.commons.elasticsearch2 {

import scala.reflect.ClassTag
import scala.reflect.classTag
import spray.json.{JsString, JsonFormat}
import spray.json.JsValue
import org.joda.time.DateTime
import com.busymachines.commons.implicits._
import com.busymachines.commons.spray.ProductField
import com.busymachines.commons.spray.ProductJsonFormat

/**
* Base class for mapping objects.
*/
abstract class ESMapping[A <: Product :ClassTag :ProductJsonFormat] {

  private var _explicitFields = Map[String, ESField[A, _]]()
  private val caseClassFields : Map[String, CaseClassField] = CaseClassFields.of[A]

  lazy val fieldsByName = _explicitFields
  lazy val fieldsByPropertyName = _explicitFields.values.groupBy(_.propertyName).mapValues(_.head)
//  lazy val jsonFormat = new ProductFormat[A](_explicitFields.values.map(f => ProductFormatField(f.propertyName, Some(f.name), None, Some(f.jsonFormat)))) {}

  lazy val fields = _explicitFields.values
  lazy val mappingName = classTag[A].runtimeClass.getName.stripSuffix("$")

  // Mapping options
  protected var ttl : Option[Duration] = None // enable ttl without a default ttl value: Some(Duration.Inf)

  // Field types
  private implicit def fromSringTuple(t: (String, String)) = Seq(new ESFieldOption(t._1, JsString(t._2)))
  protected object String extends FieldType[String]("type" -> "string")
  protected object Float extends FieldType[Float]("type" -> "float")
  protected object Double extends FieldType[Double]("type" -> "double")
  protected object Byte extends FieldType[Byte]("type" -> "byte")
  protected object Short extends FieldType[Short]("type" -> "short")
  protected object Integer extends FieldType[Int]("type" -> "integer")
  protected object Long extends FieldType[Long]("type" -> "long")
  protected object TokenCount extends FieldType[Int]("type" -> "token_count")
  protected object Date extends FieldType[DateTime]("type" -> "date")
  protected object Boolean extends FieldType[Boolean]("type" -> "boolean")
  protected object Binary extends FieldType[Array[Byte]]("type" -> "binary")

  protected def Object[B :ClassTag :JsonFormat]() =
    new FieldType[B]("type" -> "object", None)

  protected def Nested[B <: Product :ClassTag :JsonFormat](implicit mapping: ESMapping[B]) =
    new FieldType[B]("type" -> "nested", Some(mapping))

  // Field options
  protected object Analyzed extends ESFieldOption("index", JsString("analyzed"))
  protected object NotAnalyzed extends ESFieldOption("index", JsString("not_analyzed"))

  private[elasticsearch2] class FieldType[T :ClassTag :JsonFormat](options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T with Product]] = None) {
    def as[T2](implicit jsonFormat: JsonFormat[T2], classTag: ClassTag[T2]) =
      if (childMapping.isDefined) throw new Exception("Can't convert a field with a child mapping to another type")
      else new FieldType[T2](options, None)
    def ::(name: String) = new Field(name, name, options, childMapping)
    def ::(name: (String, String)) = new Field(name._1, name._2, options, childMapping)
    class Field(name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T with Product]])
      extends ESField[A, T](name, propertyName, options, childMapping)(implicitly[ClassTag[T]], implicitly[JsonFormat[T]]) {
      _explicitFields += (name -> this)
      def & (option: ESFieldOption) = new Field(name, propertyName, options :+ option, childMapping)
    }
  }

  def toMappingDefinition: JsObject = {
    // Check mapping
    checkValid.toSeq.isEmptyOrElse(errors => throw new Exception(s"Mapping errors found:\n  ${errors.mkString("\n  ")}"))

    JsObject(
      "_all" -> JsObject("enabled" -> JsTrue) ::
      "_source" -> JsObject("enabled" -> JsTrue) ::
      "store" -> JsTrue ::
      "properties" -> toProperties ::
      ttl.toList.map {
        case ttl if ttl.isFinite => "_ttl" -> JsObject("enabled" -> JsTrue, "default" -> JsNumber(ttl.toMillis))
        case ttl => "_ttl" -> JsObject("enabled" -> JsTrue)
      })
  }

  private def toProperties : JsObject =
    JsObject(_explicitFields.values.map(f =>
      f.name -> JsObject(
        f.options.map(o => o.name -> o.value) ++
        f.childMapping.flatMap("properties" -> _.toProperties) toMap)).toMap)

  private def checkValid : Iterable[String] = {
    // All properties should have a mapping field
    caseClassFields.keys.flatMap { propertyName =>
      if (fieldsByPropertyName.contains(propertyName)) Seq.empty
      else Seq(s"No field defined for 'propertyName' in mapping $mappingName")
    } ++
    // All options should be unique
    fields.flatMap(f => f.options.groupBy(_.name).flatMap {
      case (name, values) if values.size > 1 => Seq(s"Mapping $mappingName.${f.name} contains incompatible options: " + values.mkString(", "))
      case _ => Seq.empty
    }) ++
    // Recurse
    fields.flatMap(_.childMapping.map(_.checkValid).getOrElse(Nil))
  }
}

sealed trait ESPath[A, T] {
  def fields: Seq[ESField[_, _]]
  def /[T2](field: ESField[T, T2]) = ESMultiPath[A, T2](fields :+ field)
  def ++[T2] (other : ESPath[T, T2]) = ESMultiPath[A, T2](fields ++ other.fields)

  def equ(value: T) = ESCriteria.Equ(this, value)

  override def toString = fields.map(_.name).mkString(".")
}

object ESPath {
  implicit def fromExt[A, E, T](path: ESPath[E, T])(implicit e: com.busymachines.commons.Extension[A, E]) = path.asInstanceOf[ESPath[A, T]]
}

case class ESMultiPath[A, T](fields: Seq[ESField[_, _]]) extends ESPath[A, T]

case class ESField[A, T] protected (name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T with Product]])(implicit val classTag: ClassTag[T], val jsonFormat: JsonFormat[T])
  extends ESPath[A, T] { def fields = Seq(this) }

object ESField {
  implicit def fromExt[A, E, T](field: ESField[E, T])(implicit e: com.busymachines.commons.Extension[A, E]) = field.asInstanceOf[ESField[A, T]]
}

case class ESFieldOption(name: String, value: JsValue)



}
package test {
import com.busymachines.commons.elasticsearch2._
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.implicits._
import spray.json.StandardFormats
import com.busymachines.commons.implicits._
import com.busymachines.commons.domain.Id
import com.busymachines.commons.Extension
import scala.reflect.ClassTag
import com.busymachines.commons.spray.ProductJsonFormat

case class ThingBox(things: List[Thing])
case class Thing(name: String, extensions: Extensions[Thing])
case class BigThing(size: Int)

object Implicits {
implicit val thingFormat = format2(Thing)
implicit val thingBoxFormat = format1(ThingBox)
implicit val bigThingFormat = format1(BigThing)

  abstract class ESMapping2[A <: Product :ClassTag :ProductJsonFormat, Type] extends ESMapping[A]

  implicit object ThingMapping extends ESMapping2[Thing, Thing.type] {
    val name = "name" :: String & Analyzed
    val owner = "owner" :: String.as[Id[Party]] & Analyzed
  }

  implicit def thingMapping(t: Thing.type) = ThingMapping
  implicit def bigThingMapping(t: BigThing.type) = BigThingMapping
  implicit def thingBoxMapping(t: ThingBox.type) = ThingBoxMapping


    implicit object ThingBoxMapping extends ESMapping2[ThingBox, ThingBox.type] {
    val things = "things" :: Nested[Thing]
  }


  object BigThingMapping extends ESMapping2[BigThing, BigThing.type] {
    val size = "size" :: Integer & Analyzed
  }

//  implicit def toMapping1[A, F1](companion: F1 => A)(implicit mapping: ESMapping[A]) = mapping
//  implicit def toMapping1[A <: ThingBox, F1, M <: ESMapping[A]](companion: ThingBox.type)(implicit mapping: M) = mapping
}

import Implicits._



object App {

  implicit object BigThingExtension extends Extension[Thing, BigThing](_.extensions, (a, b) => a.copy(extensions = b))


//  implicit def toMapping1[M <: ESMapping[ThingBox]](companion: ThingBox.type)(implicit mapping: M) = mapping

  //  implicit def toBigThing(thing: Thing) = new BigThing(100)
  val p1: ESField[BigThing, Int] = BigThing.size
  val p2: ESPath[BigThing, Int] = BigThing.size
  val p3: ESPath[ThingBox, String] = ThingBox.things / Thing.name
  val p3x: ESPath[ThingBox, String] = ThingBox.things / Thing.name
  val p4: ESField[Thing, Int] = BigThing.size
  val p5: ESPath[ThingBox, Int] = ThingBox.things / BigThing.size
  val p6: ESPath[ThingBox, Int] = ThingBox.things ++ BigThing.size
}
}