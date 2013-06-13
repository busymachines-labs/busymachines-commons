package com.busymachines.commons.dao.elasticsearch

import com.busymachines.commons.RichJsValue.recurse

import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue

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

  def convertToES(mapping : Mapping) : JsValue = 
    convertToES(mapping.allProperties)
    
  def convertToES(properties : Mapping.Properties) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByMappedName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.name -> new RichJsValue(field._2).convertToES(properties)
                case None =>
                  property.name -> field._2
              }
            case None => field
          }
        })
      case value => value
    }
  }
  
  def convertFromES(mapping : Mapping) : JsValue = 
    convertFromES(mapping.allProperties)
    
  def convertFromES(properties : Mapping.Properties) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field => properties.propertiesByName.get(field._1) match {
            case Some(property) =>
              property.nestedProperties match {
                case Some(properties) =>
                  property.mappedName -> new RichJsValue(field._2).convertFromES(properties)
                case None =>
                  property.mappedName -> field._2
              }
            case None => field
          }
        })
      case value => value
    }
  }
}

class RichJsValue2(val value : JsValue) {
  
  def withESIds: JsValue = recurse(value) {
    case ("id", value) => ("_id", value)
  }
  def withoutESIds: JsValue = recurse(value) {
    case ("_id", value) => ("id", value)
  }
}
