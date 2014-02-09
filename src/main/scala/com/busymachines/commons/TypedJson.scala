package com.busymachines.commons

import _root_.spray.json.JsonFormat

/**
 * Created by ruud on 08/02/14.
 */
class TypedJsValue[A](val value: A)(implicit format: JsonFormat[A]) {

  def isEmpty: Boolean = false
}

object TypedJsValue {
  implicit def toTypedJsValue[A](value: A)(implicit format: JsonFormat[A]) = new TypedJsValue[A](value)(format)
  implicit def toValue[A](typedJsValue: TypedJsValue[A]) = typedJsValue.value
}

trait TypedJsValueFormats {
//  implicit def typedJsValue[A](value: TypedJsValue[A])(implicit format: JsonFormat[A])
}