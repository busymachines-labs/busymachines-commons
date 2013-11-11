package com.busymachines.commons.elasticsearch

import spray.json.JsValue
import spray.json.JsString
import spray.json.JsNumber
import org.joda.time.DateTime

object JsValueConverter {
  implicit val stringConverter = new JsValueConverter[String] {
    def convert(value: JsValue): String =
      value match {
        case JsString(s) => s
        case other => other.toString
      }
  }

  implicit val doubleConverter = new JsValueConverter[Double] {
    def convert(value: JsValue): Double =
      value match {
        case JsNumber(n) => n.toDouble
        case other => other.toString.toDouble
      }
  }

  implicit val booleanConverter = new JsValueConverter[Boolean] {
    def convert(value: JsValue): Boolean =
      value match {
        case JsString(s) => s.toBoolean
        case other => false
      }
  }

  implicit val dateConverter = new JsValueConverter[DateTime] {
    def convert(value: JsValue): DateTime =
      value match {
        case JsString(s) => DateTime.parse(s)
        case other => throw new IllegalArgumentException(s"Cannot convert string $other to date time")
      }
  }

  // TODO: other ES types
}

trait JsValueConverter[T] {
  def convert(value: JsValue): T
}
