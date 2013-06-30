package com.busymachines.commons.dao.elasticsearch

import com.busymachines.commons.RichJsValue.recurse
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonWriter
import spray.json.JsonReader

class RichJsValue(val value : JsValue) extends AnyVal {
  def getESVersion: String = value match {
    case obj : JsObject =>
      obj.fields.get("_version").map(_.toString).getOrElse("")
    case _ => ""
  }
  def withESVersion(version : String) = value match {
    case obj : JsObject =>
      JsObject(obj.fields + ("_version" -> JsString(version)))
    case value => value
  }

  def mapToES[A](mapping : Mapping[A]) : JsValue = 
    RichJsValue.convertToES(value, mapping.allProperties)

  def mapFromES[A](mapping : Mapping[A]) : JsValue = 
    RichJsValue.convertFromES(value, mapping.allProperties)

  def convertFromES[A](mapping : Mapping[A])(implicit reader : JsonReader[A]) : A = 
    RichJsValue.convertFromES(value, mapping.allProperties).convertTo[A]
}

object RichJsValue {
  private [elasticsearch] def convertToES(value: JsValue, properties : Mapping.Properties[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.name -> convertToES(field._2, properties)
                case None =>
                  property.mappedName -> field._2
              }
            case None => field
          }
        })
      case value => value
    }
  }
 
  private [elasticsearch] def convertFromES(value : JsValue, properties : Mapping.Properties[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByMappedName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.name -> convertFromES(field._2, properties)
                case None =>
                  property.name -> field._2
              }
            case None => field
          }
        })
      case value => value
    }
  }}

class RichJsValue2(val value : JsValue) {
  
  def withESIds: JsValue = recurse(value) {
    case ("id", value) => ("_id", value)
  }
  def withoutESIds: JsValue = recurse(value) {
    case ("_id", value) => ("id", value)
  }
}
