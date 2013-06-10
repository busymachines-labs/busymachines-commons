package com.busymachines.commons.elasticsearch

import com.busymachines.commons.JsonUtils.recurse
import spray.json.JsValue
import spray.json._
import org.elasticsearch.action.update.UpdateRequest
import spray.json.JsObject
import org.elasticsearch.common.bytes.BytesArray
import spray.json.JsString
import org.elasticsearch.common.base.Charsets
import org.elasticsearch.search.SearchHit
import com.busymachines.commons.dao.Versioned
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait ESImplicits {

  implicit class ESRichJsValue(value: JsValue) {
    def withESIds: JsValue = recurse(value) {
      case ("id", value) => ("_id", value)
    }
    def withoutESIds: JsValue = recurse(value) {
      case ("_id", value) => ("id", value)
    }
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
  }
  implicit def stripVersionedFromFuture[T](f : Future[Versioned[T]])(implicit ec : ExecutionContext) = f.map(_.entity)
  implicit def stripVersionedFromFutureOption[T](f : Future[Option[Versioned[T]]])(implicit ec : ExecutionContext) = f.map(_.map(_.entity))
  implicit def stripVersionedFromFutureList[T](f : Future[List[Versioned[T]]])(implicit ec : ExecutionContext) = f.map(_.map(_.entity))
  implicit def convertToUnit(f : Future[_])(implicit ec : ExecutionContext) : Future[Unit] = f.map(_ => Unit)
  implicit class ESRichUpdateRequest(request : UpdateRequest) {
    def source(source : String) = 
      request.source(new BytesArray(source.getBytes(Charsets.UTF_8)))
  }
}

package object implicits extends ESImplicits
