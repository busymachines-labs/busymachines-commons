package com.busymachines.commons.elasticsearch

import spray.json.JsValue
import spray.json.JsString

object JsValueConverter {
  implicit val stringConverter = new JsValueConverter[String] {
    def convert(value : JsValue) : String = 
    value match {
    	case JsString(s) => s
    	case other => other.toString
    }
  }

  implicit val booleanConverter = new JsValueConverter[Boolean] {
    def convert(value : JsValue) : Boolean = 
    value match {
    	case JsString(s) => s toBoolean
    	case other => false
    }
  }

  // TODO: other ES types
}

trait JsValueConverter[T] {
    def convert(value : JsValue) : T
}
