package com.busymachines.commons.spray

import _root_.spray.json._
import java.lang.reflect.{ParameterizedType, Modifier}
import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * A field format allows
 */
trait ProductFieldFormat[F] {
  def write(field: ProductField, value: F, rest: List[JsField]) : List[JsField]
  def read(field: ProductField, obj: JsObject) : F
  def withJsonName(jsonName: String) = this
  def withJsonFormat(format: JsonFormat[F]) = this
  def withDefault(default: Option[() => Any]) = this
}

object ProductFieldFormat {
  implicit def of[F](implicit jsonFormat: JsonFormat[F]) =
    DefaultProductFieldFormat[F](None, None, jsonFormat)
  implicit def fromUnit(unit: Unit) =
    NullProductFieldFormat
}

class NullProductFieldFormat[F] extends ProductFieldFormat[F] {
  def write(field: ProductField, value: F, rest: List[JsField])  = rest
  def read(field: ProductField, obj: JsObject) : F =
    field.default.getOrElse(throw new IllegalStateException(s"Field ${field.name} should have a default value")).apply().asInstanceOf[F]
}

object NullProductFieldFormat extends NullProductFieldFormat[Any]

case class DefaultProductFieldFormat[F](jsonName: Option[String], default: Option[() => Any], jsonFormat: JsonFormat[F]) extends ProductFieldFormat[F] {
  def write(field: ProductField, value: F, rest: List[JsField]) =
    if (field.isOption && value == None) rest
    else if (field.isSeq && field.default.isDefined && value == Seq.empty) rest
    else if (field.isMap && field.default.isDefined && value == Map.empty) rest
    else jsonName.getOrElse(field.name) -> jsonFormat.write(value) :: rest

  def read(field: ProductField, obj: JsObject) =
    obj.fields.get(jsonName.getOrElse(field.name)) match {
      case Some(value) => jsonFormat.read(value)
      case None => default.orElse(field.default) match {
        case Some(defarg) => defarg().asInstanceOf[F]
        case None =>
          if (field.isOption) None.asInstanceOf[F]
          else deserializationError("Object is missing required member '" + field.name + "'")
      }
    }
  override def withJsonName(jsonName: String) = this.copy(jsonName = Some(jsonName))
  override def withDefault(default: Option[() => Any]) = this.copy(default = default)
  override def withJsonFormat(format: JsonFormat[F]) = this.copy(jsonFormat = format)
}

case class ProductField(
 name: String,
 default: Option[() => Any] = None,
 isOption: Boolean = false,
 isSeq: Boolean = false,
 isMap: Boolean = false,
 fieldType: Class[_],
 genericParameterTypes: Array[Class[_]],
 format: ProductFieldFormat[_])

abstract class ProductFormat[P :ClassTag] extends RootJsonFormat[P] { outer =>

  val fields: Array[ProductField]
  protected val delegate: ProductFormat[P]

  def write(p: P) = delegate.write(fields, p)
  def read(value: JsValue) = delegate.read(fields, value)
  protected def write(fields: Seq[ProductField], p: P) : JsValue
  protected def read(fields: Seq[ProductField], value: JsValue) : P

  def withJsonNames(jsonNames: (String, String)*) = decorate(
    fields.map(f => jsonNames.find(_._1 == f.name).map(s => f.copy(format = f.format.withJsonName(s._2))).getOrElse(f)))

  def withJsonFormats(jsonFormats: (String, JsonFormat[_])*) = decorate(
    fields.map(f => jsonFormats.find(_._1 == f.name).map(s => f.copy(format = f.format.asInstanceOf[ProductFieldFormat[Any]].withJsonFormat(s._2.asInstanceOf[JsonFormat[Any]]))).getOrElse(f)))

  def withDefaults(defaults: (String, () => Any)*) = decorate(
    fields.map(f => f.copy(default = defaults.find(_._1 == f.name).map(_._2).orElse(f.default))))

  def withFieldFormats(formats: (String, ProductFieldFormat[_])*) = decorate(
    fields.map(f => f.copy(format = formats.find(_._1 == f.name).map(_._2).getOrElse(f.format))))

  def withExcludedFields(fields: String*) =
    withFieldFormats(fields.map(_ -> NullProductFieldFormat):_*)

  def mapFields(cp: ProductField => ProductField) = decorate(
    fields.map(cp))

  private def decorate(_fields: Array[ProductField]) = new ProductFormat[P] {
    val fields = _fields
    val delegate = outer.delegate
    def write(fields: Seq[ProductField], p: P) : JsValue = throw new IllegalStateException
    def read(fields: Seq[ProductField], value: JsValue) : P = throw new IllegalStateException
  }
}

abstract private[spray] class ProductFormatImpl[P <: Product :ClassTag, F0 :ProductFieldFormat, F1 :ProductFieldFormat, F2 :ProductFieldFormat,F3 :ProductFieldFormat,F4 :ProductFieldFormat,F5 :ProductFieldFormat,F6 :ProductFieldFormat,F7 :ProductFieldFormat,F8 :ProductFieldFormat,F9 :ProductFieldFormat,F10 :ProductFieldFormat, F11 :ProductFieldFormat, F12 :ProductFieldFormat, F13 :ProductFieldFormat, F14 :ProductFieldFormat, F15 :ProductFieldFormat, F16 :ProductFieldFormat, F17 :ProductFieldFormat, F18 :ProductFieldFormat, F19 :ProductFieldFormat, F20 :ProductFieldFormat, F21 :ProductFieldFormat] extends ProductFormat[P] {

  protected val delegate = this

  protected def write[F :ProductFieldFormat](field: ProductField, p: P, fieldIndex: Int, rest: List[JsField]): List[JsField] =
    field.format.asInstanceOf[ProductFieldFormat[Any]].write(field, p.productElement(fieldIndex), rest)

  protected def read[F :ProductFieldFormat](field: ProductField, value: JsValue) : F = {
    value match {
      case obj: JsObject => field.format.read(field, obj).asInstanceOf[F]
      case _ => deserializationError("Object expected")
    }
  }

  private def fmt[F](implicit f: ProductFieldFormat[F]) = f.asInstanceOf[ProductFieldFormat[Any]]

  val fields = {
    val formats = Array(fmt[F0], fmt[F1],
      fmt[F2], fmt[F3], fmt[F4], fmt[F5], fmt[F6],
      fmt[F7], fmt[F8], fmt[F9], fmt[F10], fmt[F11],
      fmt[F12], fmt[F13], fmt[F14], fmt[F15], fmt[F16],
      fmt[F17], fmt[F18], fmt[F19], fmt[F20], fmt[F21])

    val runtimeClass = classTag[P].runtimeClass
    try {
      // Need companion class for default arguments.
      lazy val companionClass = Class.forName(runtimeClass.getName + "$")
      lazy val moduleField =
        try { companionClass.getField("MODULE$") }
        catch { case e : Throwable => throw new RuntimeException("Can't deserialize default arguments of nested case classes", e) }
      lazy val companionObj = moduleField.get(null)
      // copy methods have the form copy$default$N(), we need to sort them in order, but must account for the fact
      // that lexical sorting of ...8(), ...9(), ...10() is not correct, so we extract N and sort by N.toInt
      val copyDefaultMethods = runtimeClass.getMethods.filter(_.getName.startsWith("copy$default$")).sortBy(
        _.getName.drop("copy$default$".length).takeWhile(_ != '(').toInt)
      val fields = runtimeClass.getDeclaredFields.filterNot(f => f.getName.startsWith("$") || Modifier.isTransient(f.getModifiers))
      if (copyDefaultMethods.length != fields.length)
        sys.error("Case class " + runtimeClass.getName + " declares additional fields")
      val applyDefaultMethods = copyDefaultMethods.map { method =>
        try {
          val defmeth = companionClass.getMethod("apply" + method.getName.drop("copy".size))
          Some(() => defmeth.invoke(companionObj))}
        catch { case e : Throwable => None }
      }
      if (fields.zip(copyDefaultMethods).exists { case (f, m) => f.getType != m.getReturnType })
        sys.error("Cannot determine field order of case class " + runtimeClass.getName)
      fields.zip(applyDefaultMethods).zipWithIndex.map { case ((f, m), index) =>
        val typeArgs: Array[Class[_]] = f.getGenericType match {
          case pType: ParameterizedType => pType.getActualTypeArguments.collect {
            case argClass: Class[_] => argClass
          }
          case _ => Array.empty
        }
        ProductField(f.getName, default = m, classOf[Option[_]].isAssignableFrom(f.getType), classOf[Seq[_]].isAssignableFrom(f.getType), classOf[Map[_, _]].isAssignableFrom(f.getType), f.getType, typeArgs, format = formats(index))
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + runtimeClass.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }
}
