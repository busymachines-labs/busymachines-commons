package com.busymachines.commons

import _root_.spray.json._
import java.lang.reflect.Modifier
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.Some

case class ProductFormatField(name: String, jsonName: Option[String] = None, default: Option[() => Any] = None, jsonFormat: Option[JsonFormat[_]] = None)

object ProductFormat {

  case class Field(name: String, jsonName: Option[String] = None, default: Option[() => Any] = None, jsonFormat: Option[JsonFormat[_]], isOption: Boolean = false, isSeq: Boolean = false, isMap: Boolean = false, isExtension: Boolean = false)

  protected def extractFields[P <: Product :ClassTag]: Array[Field] = {
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
        Field(f.getName, jsonName = None, default = m, jsonFormat = None, classOf[Option[_]].isAssignableFrom(f.getType), classOf[Seq[_]].isAssignableFrom(f.getType), classOf[Map[_, _]].isAssignableFrom(f.getType), classOf[Extensions].isAssignableFrom(f.getType))
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }
}

abstract class ProductFormat[P <: Product :ClassTag](fieldOverrides: Iterable[ProductFormatField] = Nil, calculatedFields: P => Iterable[JsField] = (_: P) => Nil) extends RootJsonFormat[P] {

  import ProductFormat._

  protected val fields = for {
    f <- extractFields
    update = fieldOverrides.find(_.name == f.name).getOrElse(ProductFormatField(f.name))
  } yield f.copy(jsonName = update.jsonName.orElse(f.jsonName), default = update.default.orElse(f.default), jsonFormat = update.jsonFormat.orElse(f.jsonFormat))


  protected def toJson[F](field: Field, p: Product, fieldIndex: Int, rest: List[JsField])(implicit writer: JsonWriter[F]): List[JsField] = {
    val value = p.productElement(fieldIndex)
    if (field.isOption && value == None) rest
    else if (field.isSeq && field.default.isDefined && value == Seq.empty) rest
    else if (field.isMap && field.default.isDefined && value == Map.empty) rest
    else if (field.isExtension) toJson(value.asInstanceOf[Extensions]) ++ rest
    else (field.name, writer.write(value.asInstanceOf[F])) :: rest
  }

  private def toJson(extensions: Extensions): List[JsField] = {
    extensions.map.toList.flatMap {
      case (e, a) =>
        e.format.asInstanceOf[JsonFormat[Any]].write(a.asInstanceOf[Any]) match {
          case JsObject(fields) => fields.toList
          case value => Nil
        }
    }
  }

  protected def fromJson[P :ClassTag, F :JsonReader](field: Field, value: JsValue) : F = {
    if (!field.isExtension) {
      value match {
        case x: JsObject =>
          x.fields.get(field.name) match {
            case Some(value) =>
              implicitly[JsonReader[F]].read(value)
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
    } else {
      new Extensions(Extensions.registeredExtensions.keys.filter(_.parentClass == scala.reflect.classTag[P]).map { e =>
        e.asInstanceOf[Extension[P, _]] -> e.format.read(value)
      }.toMap).asInstanceOf[F]

    }
  }
}

trait CommonProductFormats { self: StandardFormats =>

  type JFmt[A] = JsonFormat[A]

  implicit val extensionsFormat = new RootJsonFormat[Extensions] {
    override def write(obj: Extensions): JsValue = JsNull
    override def read(json: JsValue): Extensions = Extensions.empty
  }

  def jsonFormat1a[P <: Product :ClassTag, F1 :JFmt](construct: (F1) => P) = new ProductFormat[P] {
    def write(p: P) = JsObject(
      toJson[F1](fields(0), p, 0,
          Nil)
    )
    def read(value: JsValue) = construct(
      fromJson[P, F1](fields(0), value)
    )
  }

  def jsonFormat2a[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt](construct: (F1, F2) => P) = new ProductFormat[P] {
    def write(p: P) = JsObject(
      toJson[F1](fields(0), p, 0,
        toJson[F2](fields(1), p, 1,
          Nil))
    )
    def read(value: JsValue) = construct(
      fromJson[P, F1](fields(0), value),
      fromJson[P, F2](fields(1), value)
    )
  }
}
