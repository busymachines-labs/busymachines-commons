import com.busymachines.commons.Extensions
import scala.concurrent.duration.Duration
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsTrue

package com.busymachines.commons.elasticsearch2 {

import com.busymachines.commons.implicits._
import com.busymachines.commons.spray.DefaultProductFieldFormat
import com.busymachines.commons.spray.ProductField
import com.busymachines.commons.spray._
import com.busymachines.commons.{ExtensionsProductFieldFormat, Extension}
import org.joda.time.DateTime
import scala.Some
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scala.reflect.classTag
import spray.json.JsValue
import spray.json.{JsString, JsonFormat}

/**
* Base class for mapping objects.
*/
abstract class ESMapping[A :ClassTag :ProductFormat] {

  private val classTag = implicitly[ClassTag[A]]
  private val originalFormat = implicitly[ProductFormat[A]]

  private val registeredExtensions = new TrieMap[Extension[A, _], ESMapping[_]]

  /**
   * Will be filled during construction of the mapping object.
   */
  private var _explicitFields = Map[String, ESField[A, _]]()

  private lazy val mappingName = classTag.runtimeClass.getName.stripSuffix("$")

  /**
   * Mapped fields.
   */
  lazy val fields = _explicitFields.values
  lazy val fieldsByName = _explicitFields
  lazy val fieldsByPropertyName = _explicitFields.values.groupBy(_.propertyName).mapValues(_.head)

  /**
   * Use this format to convert case classes to and from json.
   */
  lazy val format: ProductFormat[A] = {
    val errors = new ArrayBuffer[String]
    val result = format(errors)
    if (errors.nonEmpty)
      throw new Exception(s"Errors in mapping $mappingName: \n  ${errors.mkString("\n  ")}")
    result
  }

  /**
   * Call this method during application start-up to register mappings for extensions
   */
  def registerExtension[E](extensionMapping: ESMapping[E])(implicit extension: Extension[A, E]) {
    registeredExtensions.put(extension, extensionMapping)
  }

  /**
   * Retrieves the mapping definition to be passed to ES during initialization of a root dao.
   */
  def mappingDefinition(docType: String): JsObject = {
    // Eager mapping validation
    format

    JsObject(docType ->
      JsObject(
        "_all" -> JsObject("enabled" -> JsTrue) ::
        "_source" -> JsObject("enabled" -> JsTrue) ::
        "store" -> JsTrue ::
        "properties" -> toProperties ::
          ttl.toList.map {
            case ttl if ttl.isFinite => "_ttl" -> JsObject("enabled" -> JsTrue, "default" -> JsNumber(ttl.toMillis))
            case ttl => "_ttl" -> JsObject("enabled" -> JsTrue)
          }))
  }

  /**
   * Option to set time-to-live for documents of this mapping. Only applies to root-level
   * documents. To enable ttl without a default ttl value, specifiy Some(Duration.Inf)
   */
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

  /**
   * Object type: values are stored as a embedded objects, but individual fields
   * are not recognized by ES.
   */
  protected def Object[B :ClassTag :JsonFormat]() =
    new FieldType[B]("type" -> "object", None)

  /**
   * Nested type: values are stored as nested objects, the fields of which should be mapped
   * using given mapping.
   */
  protected def Nested[B](mapping: ESMapping[B]) =
    new FieldType[B]("type" -> "nested", Some(mapping), isNested = true)(mapping.classTag, mapping.originalFormat)

  // Field options
  protected object Analyzed extends ESFieldOption("index", JsString("analyzed"))
  protected object NotAnalyzed extends ESFieldOption("index", JsString("not_analyzed"))

  private def format(errors: ArrayBuffer[String]): ProductFormat[A] = {
    val extensions = registeredExtensions.toMap
    val originalFormat = implicitly[ProductFormat[A]]
    // Check that all fields have a corresponding case class field
    for (field <- fieldsByName.values if !field.isDerived)
      if (!originalFormat.fields.exists(_.name == field.propertyName))
        errors.append(s"Mapped field $mappingName.${field.propertyName} has no corresponding property in its case class.")
    // Check that all extensions have a mapping
    for (ext <- Extensions.registeredExtensionsOf[A])
      if (!registeredExtensions.contains(ext))
        errors.append(s"No mapping registered for extension $ext")
    // Check that all extensions mapping have a registered mapping
    for (ext <- registeredExtensions.keys)
      if (!Extensions.registeredExtensionsOf[A].contains(ext))
        errors.append(s"Extension was not registered: $ext")
    // All options should be unique
    for (field <- fields; options <- field.options.groupBy(_.name).values)
      if (options.size > 1) errors.append(s"Mapping $mappingName.${field.name} contains incompatible options: ${options.mkString(", ")}")
    // Create a new format with json names and formats from mappings
    originalFormat.mapFields { field =>
      field.copy(format = field.format match {
        case extFormat : ExtensionsProductFieldFormat[_] =>
          new ExtensionsProductFieldFormat[A] {
            override def formatOf[E](ext: Extension[A, E]) =
              extensions.getOrElse(ext, throw new Exception(s"No mapping registered for extension $ext")).format(errors).asInstanceOf[ProductFormat[E]]
          }
        case format =>
          fieldsByName.get(field.name).orElse(determineMappingField(field, errors)) match {
            case Some(mappingField) =>
              val newFormat = mappingField.childMapping.map(_.format).getOrElse(mappingField.jsonFormat).asInstanceOf[JsonFormat[Any]]
              format.asInstanceOf[ProductFieldFormat[Any]].withJsonName(mappingField.name).withJsonFormat(newFormat)
            case None =>
              format
          }
      })
    }
  }

  private def determineMappingField(field: ProductField, errors: ArrayBuffer[String]): Option[ESField[_, _]] = {
    field.format match {
      case DefaultProductFieldFormat(jsonName, default, jsonFormat) =>
        if (jsonFormat.isInstanceOf[ProductFormat[_]])
          errors.append(s"Field ${field.name} should be mapped explicitly in $mappingName")
        val runtimeClass = if (field.isSeq) field.genericParameterTypes(0) else field.fieldType
        val fieldType = if (runtimeClass == classOf[Float]) Float
        else if (runtimeClass == classOf[Double]) Double
        else if (runtimeClass == classOf[Short]) Short
        else if (runtimeClass == classOf[Integer]) Integer
        else if (runtimeClass == classOf[Long]) Long
        else if (runtimeClass == classOf[DateTime]) Date
        else if (runtimeClass == classOf[Boolean]) Boolean
        else if (runtimeClass == classOf[Array[Byte]]) Binary
        else String
        Some(jsonName.getOrElse(field.name) -> field.name :: fieldType & NotAnalyzed)
      case _ => None
    }
  }

  private def toProperties : JsObject =
    JsObject((_explicitFields.values ++ registeredExtensions.values.flatMap(_._explicitFields.values.asInstanceOf[Seq[ESField[A, _]]])).map(f =>
      f.name -> JsObject(
        f.options.map(o => o.name -> o.value) ++
        f.childMapping.flatMap("properties" -> _.toProperties) toMap)).toMap)

  private[elasticsearch2] class FieldType[T :ClassTag :JsonFormat](val options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T]] = None, isNested: Boolean = false) {
    def as[T2](implicit jsonFormat: JsonFormat[T2], classTag: ClassTag[T2]) =
      if (childMapping.isDefined) throw new Exception("Can't convert a field with a child mapping to another type")
      else new FieldType[T2](options, None)
    def ::(name: String) = new Field(name, name, options, childMapping)
    def ::(name: (String, String)) = new Field(name._1, name._2, options, childMapping)
    class Field(name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T]])
      extends ESField[A, T](name, propertyName, options, isDerived = false, isNested, childMapping)(implicitly[ClassTag[T]], implicitly[JsonFormat[T]]) {
      _explicitFields += (name -> this)
      def & (option: ESFieldOption) = new Field(name, propertyName, options :+ option, childMapping)
    }
  }
}

/**
 * Mapped field.
 */
case class ESField[A, T] protected (name: String, propertyName: String, options: Seq[ESFieldOption], isDerived: Boolean, isNested: Boolean, childMapping: Option[ESMapping[_ <: T]])(implicit val classTag: ClassTag[T], val jsonFormat: JsonFormat[T])
  extends ESPath[A, T] { def fields = Seq(this) }

object ESField {
  implicit def fromExt[A, E, T](field: ESField[E, T])(implicit e: Extension[A, E]) = field.asInstanceOf[ESField[A, T]]
}

case class ESFieldOption(name: String, value: JsValue)


}

package test {

import Implicits._
import com.busymachines.commons.Extension
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch2._
import com.busymachines.commons.implicits._
import com.busymachines.commons.spray.ProductFormat
import com.busymachines.prefab.party.domain.Party
import scala.reflect.ClassTag

case class ThingBox(things: List[Thing])
case class Thing(name: String, extensions: Extensions[Thing])
case class BigThing(size: Int)

object Implicits {
implicit val thingFormat = format2(Thing)
implicit val thingBoxFormat = format1(ThingBox)
implicit val bigThingFormat = format1(BigThing)

  abstract class ESMapping2[A <: Product :ClassTag :ProductFormat, Type] extends ESMapping[A]

  object ThingMapping extends ESMapping2[Thing, Thing.type] {
    val name = "name" :: String & Analyzed
    val owner = "owner" :: String.as[Id[Party]] & Analyzed
  }

  implicit def thingMapping(t: Thing.type) = ThingMapping
  implicit def bigThingMapping(t: BigThing.type) = BigThingMapping
  implicit def thingBoxMapping(t: ThingBox.type) = ThingBoxMapping


  object ThingBoxMapping extends ESMapping2[ThingBox, ThingBox.type] {
    val things = "things" :: Nested(ThingMapping)
  }


  object BigThingMapping extends ESMapping2[BigThing, BigThing.type] {
    val size = "size" :: Integer & Analyzed
  }

//  implicit def toMapping1[A, F1](companion: F1 => A)(implicit mapping: ESMapping[A]) = mapping
//  implicit def toMapping1[A <: ThingBox, F1, M <: ESMapping[A]](companion: ThingBox.type)(implicit mapping: M) = mapping
}


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