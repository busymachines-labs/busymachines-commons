package com.busymachines.commons.elasticsearch

import spray.json.JsObject
import com.busymachines.commons.RichJsValue.recurse
import spray.json.JsString
import spray.json.JsValue
import spray.json.JsonReader
import spray.json.JsArray

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

  def mapToES[A](mapping : ESMapping[A]) : JsValue = 
    RichJsValue.mapToES(value, mapping.allProperties)

  def mapFromES[A](mapping : ESMapping[A]) : JsValue = 
    RichJsValue.mapFromES(value, mapping.allProperties)

  def convertFromES[A](mapping : ESMapping[A])(implicit reader : JsonReader[A]) : A = 
    RichJsValue.mapFromES(value, mapping.allProperties).convertTo[A]
}

object RichJsValue {
  private [elasticsearch] def mapToES(value: JsValue, properties : ESMapping.Properties[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.mappedName -> mapToES(field._2, properties)
                case None =>
                  property.mappedName -> field._2
              }
            case None => field
          }
        })
      case JsArray(elements) =>
        JsArray(elements.map(mapToES(_, properties)))
      case value => value
    }
  }
 
  private [elasticsearch] def mapFromES(value : JsValue, properties :ESMapping.Properties[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByMappedName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.name -> mapFromES(field._2, properties)
                case None =>
                  property.name -> field._2
              }
            case None => field
          }
        })
      case JsArray(elements) =>
        JsArray(elements.map(mapFromES(_, properties)))
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
