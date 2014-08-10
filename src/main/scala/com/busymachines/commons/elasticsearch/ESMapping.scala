package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.Implicits._
import com.busymachines.commons.spray.json._
import com.busymachines.commons.util.{ExtensionsProductFieldFormat, Extension, Extensions}
import org.joda.time.DateTime
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsTrue
import spray.json.JsFalse
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
  private val _explicitFields = new ArrayBuffer[ESField[A, _]]

  private[elasticsearch] lazy val mappingName = classTag.runtimeClass.getName.stripSuffix("$")

  /**
   * Mapped fields.
   */
//  lazy val fields = _explicitFields.values
  lazy val fieldsByName = _explicitFields.groupBy(_.name).mapValues(_.head)
  lazy val fieldsByPropertyName = _explicitFields.groupBy(_.propertyName).mapValues(_.head)

  // Predefined fields
  val _all = ESField[Any, String]("_all", "", String.options, false, false, None)

  /**
   * Use this format to convert case classes to and from json.
   */
  lazy val jsonFormat: ProductFormat[A] = {
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
    jsonFormat

    JsObject(docType ->
      JsObject(
        "_all" -> JsObject("enabled" -> JsTrue) ::
        "_source" -> JsObject("enabled" -> JsTrue) ::
        "dynamic" -> JsFalse ::
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
  protected object GeoPoint extends FieldType[GeoPoint]("type" -> "geo_point")

  /**
   * Object type: values are stored as a embedded objects, but individual fields
   * are not recognized by ES.
   */
  protected def Object[B :ClassTag :JsonFormat] =
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
  protected object NotIndexed extends ESFieldOption("index", JsString("no"))
  protected object IncludeInAll extends ESFieldOption("include_in_all", JsTrue)
  protected object Stored extends ESFieldOption("store", JsTrue)

  private def allOptions(field: ESField[_, _]): Seq[ESFieldOption] = {
//    field.options.find(_.name == "type") match {
//      case Some(JsString(t)) if t == "string" =>
//    }
    // Make Not analyzed default
    field.options.find(_.name == "index") match {
      case Some(_) => field.options
      case None => 
        fieldsByName.get(field.name) match {
          case Some(field) => field.options :+ NotAnalyzed
          case None => field.options :+ NotIndexed
        }
//        field.options :+ NotAnalyzed
    }
  }

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
    for (fields <- _explicitFields.groupBy(_.name).values)
      if (fields.size > 1)
        errors.append(s"Mapping $mappingName contains multiple definitions of field ${fields.head}")
    for (field <- fieldsByName.values; options <- field.options.groupBy(_.name).values)
      if (options.size > 1) errors.append(s"Mapping $mappingName.${field.name} contains incompatible options: ${options.mkString(", ")}")
    // Check incompatible options
//    for (field <- fieldsByName.values; tp <- field.options.find(_.name == "type"); idx <- field.options.find(_.name == "index")) {
//      if (tp.value != JsString("string") && idx.value != JsString("no")) errors.append(s"Invalid field mapping $field: only String fields can have Analyzed or NotAnalyzed specified.")
//    }
    // Create a new format with json names and formats from mappings
    originalFormat.mapFields { field =>
      field.copy(format = field.format match {
        case extFormat : ExtensionsProductFieldFormat[_] =>
          new ExtensionsProductFieldFormat[A] {
            override def formatOf[E](ext: Extension[A, E]) =
              extensions.getOrElse(ext, throw new Exception(s"No mapping registered for extension $ext")).format(errors).asInstanceOf[ProductFormat[E]]
          }
        case format =>
          fieldsByPropertyName.get(field.name).orElse(implicitMappingField(field, errors)) match {
            case Some(mappingField) =>
              mappingField.childMapping
                // get & case json format of child mapping
                .map(_.format(errors))
                // decorate with seq mapping
                .map(m => if (field.isSeq) listFormat(m) else if (field.isOption) optionFormat(m) else m)
                // convert to Any
                .mapTo[JsonFormat[Any]]
                // decorate field format with child mapping json format
                .map(format.asInstanceOf[ProductFieldFormat[Any]].withJsonFormat)
                // fallback to original format when there is no child mapping
                .getOrElse(format)
                // use json name from field
                .withJsonName(mappingField.name)
            case None =>
              format
          }
      })
    }
  }

  private def implicitMappingField(field: ProductField, errors: ArrayBuffer[String]): Option[ESField[_, _]] = {
    field.format match {
      case DefaultProductFieldFormat(jsonName, default, jsonFormat) =>
//          errors.append(s"Field ${field.name} should be mapped explicitly in $mappingName")
        val runtimeClass = if (field.isSeq) field.genericParameterTypes(0) else field.fieldType
        val fieldType = if (jsonFormat.isInstanceOf[ProductFormat[_]]) String
        else if (runtimeClass == classOf[Float]) Float
        else if (runtimeClass == classOf[Double]) Double
        else if (runtimeClass == classOf[Short]) Short
        else if (runtimeClass == classOf[Integer]) Integer
        else if (runtimeClass == classOf[Long]) Long
        else if (runtimeClass == classOf[DateTime]) Date
        else if (runtimeClass == classOf[Boolean]) Boolean
        else if (runtimeClass == classOf[Array[Byte]]) Binary
        else String
        Some(fieldType.implicitField(jsonName.getOrElse(field.name), field.name, Seq(NotIndexed)))
      case _ => None
    }
  }

  private def toProperties : JsObject =
    JsObject((_explicitFields ++ registeredExtensions.values.flatMap(_._explicitFields.mapTo[ESField[A, _]])).map(f =>
      f.name -> JsObject(
        allOptions(f).map(o => o.name -> o.value) ++
        f.childMapping.map("properties" -> _.toProperties) toMap)).toMap)

  private[elasticsearch] class FieldType[T :ClassTag :JsonFormat](val options: Seq[ESFieldOption], val childMapping: Option[ESMapping[_ <: T]] = None, val isNested: Boolean = false) {
    def as[T2](implicit jsonFormat: JsonFormat[T2], classTag: ClassTag[T2]): FieldType[T2] =
      if (childMapping.isDefined) throw new Exception("Can't convert a field with a child mapping to another type")
      else new FieldType[T2](options, None)
    def ::(name: String) = new Field(name, name, options, childMapping)
    def ::(name: (String, String)) = new Field(name._1, name._2, options, childMapping)
    def implicitField(name: String, propertyName: String, additionalOptions: Seq[ESFieldOption]) =
      ESField[A, T](name, propertyName, options ++ additionalOptions, false, isNested, childMapping)
    class Field(name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[_ <: T]])
      extends ESField[A, T](name, propertyName, options, isDerived = false, isNested, childMapping)(implicitly[ClassTag[T]], implicitly[JsonFormat[T]]) {
      _explicitFields += this
      def & (option: ESFieldOption) = {
        _explicitFields -= this
        new Field(name, propertyName, options :+ option, childMapping)
      }
    }
  }
}
