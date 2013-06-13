package com.busymachines.commons.dao.elasticsearch

import org.elasticsearch.client.Client
import spray.json.JsonFormat
import spray.json.JsObject
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.dao.Dao

abstract class ESDao[T <: HasId[T] : JsonFormat] extends Dao[T] {

  val typeName : String
  
  implicit class RichField(s : String) {
    def /(s2 : String) = s + "." + s2
  }
}