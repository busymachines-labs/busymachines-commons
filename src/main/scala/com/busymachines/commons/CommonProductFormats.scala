package com.busymachines.commons

import _root_.spray.json._
import java.lang.reflect.Modifier
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.Some

trait CommonProductFormats { self: StandardFormats =>

  type JFmt[A] = JsonFormat[A]
  case class Field(name: String, getDefault: Option[() => Any], option: Boolean = false, optionalSeq: Boolean = false, optionalMap: Boolean = false, extension: Boolean = false)


  def jsonFormat0[P <: Product :ClassTag](construct: () => P) = new RootJsonFormat[P] {
    def write(p: P) = JsObject()
    def read(value: JsValue) = construct()
  }

  def jsonFormat2[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt](construct: (F1, F2) => P, useDefaultValues: Boolean = false, excludeEmptyCollections: Boolean = true) = new RootJsonFormat[P] {
    val Array(f1, f2) = extractFields(classTag[P], useDefaultValues, excludeEmptyCollections)
    def write(p: P) = JsObject(
      toJson[F1](f1, p, 0,
      toJson[F2](f2, p, 1,
      Nil))
    )
    def read(value: JsValue) = construct(
      fromJson[P, F1](f1, value),
      fromJson[P, F2](f2, value)
    )
  }

  def jsonFormat3[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt](construct: (F1, F2, F3) => P, useDefaultValues: Boolean = false, excludeEmptyCollections: Boolean = true) = new RootJsonFormat[P] {
    val Array(f1, f2, f3) = extractFields(classTag[P], useDefaultValues, excludeEmptyCollections)
    def write(p: P) = JsObject(
      toJson[F1](f1, p, 0,
      toJson[F2](f2, p, 1,
      toJson[F3](f3, p, 1,
      Nil)))
    )
    def read(value: JsValue) = construct(
      fromJson[P, F1](f1, value),
      fromJson[P, F2](f2, value),
      fromJson[P, F3](f3, value)
    )
  }

  def jsonFormat4[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt](construct: (F1, F2, F3, F4) => P, useDefaultValues: Boolean = false, excludeEmptyCollections: Boolean = true) = new RootJsonFormat[P] {
    val Array(f1, f2, f3, f4) = extractFields(classTag[P], useDefaultValues, excludeEmptyCollections)
    def write(p: P) = JsObject(
      toJson[F1](f1, p, 0,
      toJson[F2](f2, p, 1,
      toJson[F3](f3, p, 1,
      toJson[F4](f4, p, 1,
      Nil))))
    )
    def read(value: JsValue) = construct(
      fromJson[P, F1](f1, value),
      fromJson[P, F2](f2, value),
      fromJson[P, F3](f3, value),
      fromJson[P, F4](f4, value)
    )
  }

  protected def extractFields(classManifest: ClassTag[_], useDefaultValues: Boolean, excludeEmptyCollections: Boolean): Array[Field] = {
    val clazz = classManifest.runtimeClass
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
      if (useDefaultValues) {
        fields.zip(applyDefaultMethods).map { case (f, m) => Field(f.getName, m, classOf[Option[_]].isAssignableFrom(f.getType), excludeEmptyCollections && classOf[Seq[_]].isAssignableFrom(f.getType), excludeEmptyCollections && classOf[Map[_, _]].isAssignableFrom(f.getType), classOf[Extensions].isAssignableFrom(f.getType)) }
      } else {
        fields.map { f => Field(f.getName, None, classOf[Option[_]].isAssignableFrom(f.getType), excludeEmptyCollections && classOf[Seq[_]].isAssignableFrom(f.getType), excludeEmptyCollections && classOf[Map[_, _]].isAssignableFrom(f.getType), classOf[Extensions].isAssignableFrom(f.getType)) }
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }

  private def toJson[F](field: Field, p: Product, fieldIndex: Int, rest: List[JsField])(implicit writer: JsonWriter[F]): List[JsField] = {
    val value = p.productElement(fieldIndex)
    if (field.option && value == None) rest
    else if (field.optionalMap && value == Map.empty) rest
    else if (field.optionalSeq && value == Seq.empty) rest
    else if (field.extension) toJson(value.asInstanceOf[Extensions]) ++ rest
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

  private def fromJson[P :ClassTag, F](field: Field, value: JsValue)(implicit reader: JsonReader[F]) : F = {
    if (!field.extension) {
      value match {
        case x: JsObject =>
          x.fields.get(field.name) match {
            case Some(value) =>
              reader.read(value)
            case None => field.getDefault match {
              case Some(defarg) =>
                defarg().asInstanceOf[F]
              case None =>
                if (field.option) None.asInstanceOf[F]
                else if (field.optionalSeq) Seq.empty.asInstanceOf[F]
                if (reader.isInstanceOf[OptionFormat[_]]) None.asInstanceOf[F]
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
