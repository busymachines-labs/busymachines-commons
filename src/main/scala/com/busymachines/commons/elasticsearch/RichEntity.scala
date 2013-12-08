package com.busymachines.commons.elasticsearch

import com.busymachines.commons.domain.HasId

import spray.json.JsValue
import spray.json.JsonWriter
import spray.json.pimpAny

class RichEntity[A <: HasId[A]](val entity : A) extends AnyVal {

  def convertToES(mapping : ESMapping[A])(implicit writer : JsonWriter[A]) : JsValue =
    RichJsValue.mapToES(entity.toJson, mapping)
}
