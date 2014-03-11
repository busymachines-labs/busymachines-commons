package com.busymachines.commons.elasticsearch2

import scala.reflect.ClassTag
import spray.json.{JsValue, JsonFormat}
import com.busymachines.commons.Extension

/**
 * Mapped field.
 */
case class ESField[A, T] protected (name: String, propertyName: String, options: Seq[ESFieldOption], isDerived: Boolean, isNested: Boolean, childMapping: Option[ESMapping[_ <: T]])(implicit val classTag: ClassTag[T], val jsonFormat: JsonFormat[T])
  extends ESPath[A, T] { def fields = Seq(this) }

object ESField {
  implicit def fromExt[A, E, T](field: ESField[E, T])(implicit e: Extension[A, E]) = field.asInstanceOf[ESField[A, T]]
}

case class ESFieldOption(name: String, value: JsValue)
