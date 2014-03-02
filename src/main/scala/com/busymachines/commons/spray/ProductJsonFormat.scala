package com.busymachines.commons.spray

import _root_.spray.json._
import java.lang.reflect.Modifier
import scala.reflect.ClassTag
import scala.reflect.classTag

trait ProductFieldJsonFormat[F] extends JsonFormat[F] {
  def write(value: F) : JsValue
  def read(value: JsValue) : F
  def writeField(name: String, value: F, rest: List[JsField]) : List[JsField] = name -> write(value) :: rest
  def readField(name: String, obj: JsObject) : Option[F] = obj.fields.get(name).map(read)
}

object ProductFieldJsonFormat {
  implicit def fromJsonFormat[F](implicit jsonFormat: JsonFormat[F]) = new ProductFieldJsonFormat[F] {
    def write(value: F) : JsValue = jsonFormat.write(value)
    def read(value: JsValue) : F = jsonFormat.read(value)
  }
}

case class ProductField(name: String, jsonName: Option[String] = None, default: Option[() => Any] = None, jsonFormat: Option[ProductFieldJsonFormat[_]], isOption: Boolean = false, isSeq: Boolean = false, isMap: Boolean = false)

object ProductJsonFormat {

  type Field = ProductField
  type Fields = Seq[Field]
  type JFmt[A] = ProductFieldJsonFormat[A]

  protected def extractFields[P :ClassTag]: Array[Field] = {
    val clazz = classTag[P].runtimeClass
    try {
      // Need companion class for default arguments.
      lazy val companionClass = Class.forName(clazz.getName + "$")
      lazy val moduleField =
        try { companionClass.getField("MODULE$") }
        catch { case e : Throwable => throw new RuntimeException("Can't deserialize default arguments of nested case classes", e) }
      lazy val companionObj = moduleField.get(null)
      // copy methods have the form copy$default$N(), we need to sort them in order, but must account for the fact
      // that lexical sorting of ...8(), ...9(), ...10() is not correct, so we extract N and sort by N.toInt
      val copyDefaultMethods = clazz.getMethods.filter(_.getName.startsWith("copy$default$")).sortBy(
        _.getName.drop("copy$default$".length).takeWhile(_ != '(').toInt)
      val fields = clazz.getDeclaredFields.filterNot(f => f.getName.startsWith("$") || Modifier.isTransient(f.getModifiers))
      if (copyDefaultMethods.length != fields.length)
        sys.error("Case class " + clazz.getName + " declares additional fields")
      val applyDefaultMethods = copyDefaultMethods.map { method =>
        try {
          val defmeth = companionClass.getMethod("apply" + method.getName.drop("copy".size))
          Some(() => defmeth.invoke(companionObj))}
        catch { case e : Throwable => None }
      }
      if (fields.zip(copyDefaultMethods).exists { case (f, m) => f.getType != m.getReturnType })
        sys.error("Cannot determine field order of case class " + clazz.getName)
      fields.zip(applyDefaultMethods).map { case (f, m) =>
        ProductField(f.getName, jsonName = None, default = m, jsonFormat = None, classOf[Option[_]].isAssignableFrom(f.getType), classOf[Seq[_]].isAssignableFrom(f.getType), classOf[Map[_, _]].isAssignableFrom(f.getType))
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }

  def jsonName(field: Field) =
    field.jsonName.getOrElse(field.name)

  def format[F: JFmt](field: Field): JFmt[F] =
    field.jsonFormat.map(_.asInstanceOf[JFmt[F]]).getOrElse(implicitly[JFmt[F]])
}

abstract class ProductJsonFormat[P :ClassTag] extends RootJsonFormat[P] { outer =>

  import ProductJsonFormat._

  val fields: Fields = ProductJsonFormat.extractFields[P]
  protected val delegate = this

  val jsonNames: Set[String] = fields.map(f => f.jsonName.getOrElse(f.name)).toSet

  def withJsonNames(jsonNames: Map[String, String]) = decorate(
    fields.map(f => f.copy(jsonName = jsonNames.get(f.name).orElse(f.jsonName))))

  def withJsonFormats(jsonFormats: Map[String, ProductFieldJsonFormat[_]]) = decorate(
    fields.map(f => f.copy(jsonFormat = jsonFormats.get(f.name).orElse(f.jsonFormat))))

  def withDefaults(defaults: Map[String, () => Any]) = decorate(
    fields.map(f => f.copy(default = defaults.get(f.name).orElse(f.default))))

  def write(p: P) = delegate.write(fields, p)
  def read(value: JsValue) = delegate.read(fields, value)
  def write(fields: Fields, p: P) : JsValue
  def read(fields: Fields, value: JsValue) : P

  protected def write[F :JFmt](field: Field, p: Product, fieldIndex: Int, rest: List[JsField]): List[JsField] = {
    val value = p.productElement(fieldIndex)
    if (field.isOption && value == None) rest
    else if (field.isSeq && field.default.isDefined && value == Seq.empty) rest
    else if (field.isMap && field.default.isDefined && value == Map.empty) rest
    else format[F](field).writeField(jsonName(field), value.asInstanceOf[F], rest)
  }

  protected def read[F :JFmt](field: Field, value: JsValue) : F = {
    value match {
      case obj: JsObject =>
        format[F](field).readField(jsonName(field), obj) match {
          case Some(value) =>
            value
          case None => field.default match {
            case Some(defarg) =>
              defarg().asInstanceOf[F]
            case None =>
              if (field.isOption) None.asInstanceOf[F]
              else deserializationError("Object is missing required member '" + field.name + "'")
          }
        }
      case _ => deserializationError("Object expected")
    }
  }

  private def decorate(_fields: Fields) = new ProductJsonFormat[P] {
    override val fields = _fields
    override val delegate = outer.delegate
    def write(fields: Fields, p: P) : JsValue = throw new IllegalStateException
    def read(fields: Fields, value: JsValue) : P = throw new IllegalStateException
  }
}
