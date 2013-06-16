package com.busymachines.commons.dao.elasticsearch

import org.elasticsearch.client.Client
import spray.json.JsonFormat
import spray.json.JsObject
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.dao.Dao

abstract class ESDao[T <: HasId[T] : JsonFormat](val typeName : String) extends Dao[T]
