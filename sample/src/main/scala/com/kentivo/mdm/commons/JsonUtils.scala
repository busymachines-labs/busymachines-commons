//package com.kentivo.mdm.commons
//
//import spray.json.JsValue
//import spray.json.JsObject
//import spray.json.JsArray
//
//object JsonUtils {
//  def recurse(value : JsValue)(pf : PartialFunction[(String, JsValue), (String, JsValue)]) : JsValue = {
//    value match {
//      case obj : JsObject =>
//        JsObject(obj.fields.map {
//          field =>
//            pf.applyOrElse(field, (field : (String, JsValue)) => field) match {
//              case (name, value) =>
//                (name, recurse(value)(pf))
//            } 
//        })
//      case array : JsArray =>
//        JsArray(array.elements.map(recurse(_)(pf)))
//      case value => value
//    }
//  }
//}