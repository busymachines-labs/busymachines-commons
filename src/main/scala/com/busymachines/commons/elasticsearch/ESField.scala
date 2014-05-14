package com.busymachines.commons.elasticsearch

import scala.reflect.ClassTag
import spray.json.{JsValue, JsonFormat}
import com.busymachines.commons.Extension
import com.busymachines.commons.Implicits._

/**
 * Mapped field.
 */
case class ESField[A, T] protected[elasticsearch] (name: String, propertyName: String, options: Seq[ESFieldOption], isDerived: Boolean, isNested: Boolean, childMapping: Option[ESMapping[_ <: T]])(implicit val classTag: ClassTag[T], val jsonFormat: JsonFormat[T])
  extends ESPath[A, T] {
  def fields = Seq(this)
}

object ESField {
//  def apply(name: String) = new AdhocField[String](name)
  implicit def fromExt[A, E, T](field: ESField[E, T])(implicit e: Extension[A, E]) = field.asInstanceOf[ESField[A, T]]
//  implicit def toESField[A, T :ClassTag](field: String) = ESField[A, T](field, "", Seq.empty, false, false, None)(scala.reflect.classTag[T], null.asInstanceOf[JsonFormat[T]])
}

case class ESFieldOption(name: String, value: JsValue)

class AdhocField[T :ClassTag :JsonFormat](name: String) extends ESField[Any, T](name, "", Seq.empty, false, false, None)
object AdhocField {
  def apply[T :ClassTag :JsonFormat](name: String) =
    new AdhocField[T](name)
}