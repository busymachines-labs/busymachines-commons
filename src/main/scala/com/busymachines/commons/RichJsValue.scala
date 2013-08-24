package com.busymachines.commons

import spray.json._
import scala.collection.mutable
import com.busymachines.commons.domain.Id
import scala.Some

object RichJsValue {
  def recurse(value : JsValue)(pf : PartialFunction[(String, JsValue), (String, JsValue)]) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.map {
          field =>
            pf.applyOrElse(field, (field : (String, JsValue)) => field) match {
              case (name, value) =>
                (name, recurse(value)(pf))
            } 
        })
      case JsArray(elements) =>
        JsArray(elements.map(recurse(_)(pf)))
      case value => 
        value
    }
  }
}

class RichJsValue(val value : JsValue) extends AnyVal {
  def recurse(pf : PartialFunction[(String, JsValue), (String, JsValue)]) : JsValue = 
    RichJsValue.recurse(value)(pf) 
  
  def replaceWithGeneratedIds : JsValue = {
    val idmap = mutable.Map[String, String]()
    findIds(value, idmap)
    replaceIds(value, idmap)
  }

  def setField(field : String, value : JsValue) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.filter(_._1 != field).toSeq.+:(field -> value):_*)
      case value =>
        value
    }
  }

  def unsetField(field : String) : JsValue = {
    value match {
      case JsObject(fields) =>
        JsObject(fields.filter(_._1 != field) )
      case value =>
        value
    }
  }


  private def findIds(value : JsValue, idmap : mutable.Map[String, String]) {
    value match {
      case JsObject(fields) =>
        fields.get("id") match { 
          case Some(JsNumber(id)) =>
            idmap += (id.toString -> Id.generate[Any].toString)
          case Some(JsString(id)) => 
            idmap += (id -> Id.generate[Any].toString)
          case _ =>
        }
        for ((field, value) <- fields)
          findIds(value, idmap)
      case JsArray(elements) =>
        for (value <- elements)
          findIds(value, idmap)
      case value =>
    }
  } 
  
  private def replaceIds(value : JsValue, idmap : mutable.Map[String, String]) : JsValue = {
    value match {
      case JsObject(fields) =>
        (fields.size, fields.get("id-ref")) match { 
          case (1, Some(JsString(ref))) => 
            JsString(idmap.get(ref).getOrElse {
              throw new RuntimeException(s"Id not found: $ref")
            })
          case (1, Some(JsNumber(ref))) => 
            JsString(idmap.get(ref.toString).getOrElse {
              throw new RuntimeException(s"Id not found: $ref")
            })
          case _ => 
            JsObject(fields.map {
              case ("id", JsString(value)) =>
                ("id", JsString(idmap.get(value.toString).getOrElse {
                  throw new RuntimeException(s"Id not found: $value")
                }))
              case (field, value) =>
                (field, replaceIds(value, idmap))
            })
        }
      case JsArray(elements) =>
        JsArray(elements.map(replaceIds(_, idmap)))
      case value =>
        value
    }
  }
}