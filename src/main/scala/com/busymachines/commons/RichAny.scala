package com.busymachines.commons

import spray.json.{JsValue, JsonFormat}
import com.busymachines.commons.implicits._

class RichAny[A](val a: A) extends AnyVal {
  def toOption(f : A => Boolean) = 
    if (f(a)) Some(a)
    else None

  def replaceWithGeneratedIds(implicit format : JsonFormat[A]) : A =
    format.read(format.write(a).replaceWithGeneratedIds)

  def setField(field : String, value : JsValue)(implicit format : JsonFormat[A]) : A =
    format.read(format.write(a).setField(field, value))

  def unsetField(field : String)(implicit format : JsonFormat[A]) : A =
    format.read(format.write(a).unsetField(field))
}