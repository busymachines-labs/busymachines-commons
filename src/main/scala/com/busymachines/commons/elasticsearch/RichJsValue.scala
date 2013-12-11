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
    RichJsValue.mapToES(value, mapping)

  def mapFromES[A](mapping : ESMapping[A]) : JsValue = 
    RichJsValue.mapFromES(value, mapping)

  def convertFromES[A](mapping : ESMapping[A])(implicit reader : JsonReader[A]) : A = 
    RichJsValue.mapFromES(value, mapping).convertTo[A]
}

object RichJsValue {
  private [elasticsearch] def mapToES(value: JsValue, mapping : ESMapping[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(mapping._allProperties.flatMap { property =>
          fields.find(_._1 == property.name) match {
            case Some((name, jsValue)) =>
              val mappedJsValue = property.nested match {
                case Some(nested) => mapToES(jsValue, nested)
                case None => jsValue
              }
              mappedJsValue match {
                case JsString(s) if s.isEmpty => None
                case JsObject(fields) if fields.isEmpty => None
                case array : JsArray if array.elements.isEmpty => None
                case jsValue => Some(property.mappedName -> jsValue)
              }
            case None => None
          }
        })
// original implementation
//        JsObject(fields.map {
//          field => mapping._propertiesByName.get(field._1) match {
//            case Some(property) =>
//              property.nested match {
//                case Some(properties) =>
//                  property.mappedName -> mapToES(field._2, properties)
//                case None =>
//                  property.mappedName -> field._2
//              }
//            case None => field
//          }
//        })
      case JsArray(elements) =>
        JsArray(elements.map(mapToES(_, mapping)))
      case value => value
    }
  }
 
  private [elasticsearch] def mapFromES(value : JsValue, mapping :ESMapping[_]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(mapping._allProperties.flatMap { property =>
          fields.find(_._1 == property.mappedName) match {
            case Some((mappedName, jsValue)) =>
              property.nested match {
                case Some(nested) =>
                  Some(property.name -> mapFromES(jsValue, nested))
                case None =>
                  Some(property.name -> jsValue)
              }
            case None =>
              mapping.caseClassFields.get(property.name).map(_.clazz) match {
                case Some(c) if classOf[String].isAssignableFrom(c) => Some(property.name -> JsString(""))
                case Some(c) if classOf[scala.collection.Map[_, _]].isAssignableFrom(c) => Some(property.name -> JsObject())
                case Some(c) if classOf[scala.collection.Iterable[_]].isAssignableFrom(c) => Some(property.name -> JsArray())
                case Some(c) if classOf[Option[_]].isAssignableFrom(c) => None
                case _ if property.`type` == "string" => Some(property.name -> JsString(""))
                case _ => throw new Exception(s"No value found for mandatory property '${property.mappedName}' in mapping '${mapping._mappingName}' in $value")
              } 
          }
        })
// original implementation:
        
//        JsObject(fields.map { field => 
//          mapping._propertiesByMappedName.get(field._1) match {
//            case Some(property) =>
//              property.nested match {
//                case Some(nested) =>
//                  property.name -> mapFromES(field._2, nested)
//                case None =>
//                  property.name -> field._2
//              }
//            case None => field
//          }
//        })
      case JsArray(elements) =>
        JsArray(elements.map(mapFromES(_, mapping)))
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
