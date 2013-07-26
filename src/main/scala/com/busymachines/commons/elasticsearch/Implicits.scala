package com.busymachines.commons.elasticsearch

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.Client

import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.HasId

import spray.json.JsValue

package object implicits extends ESImplicits

trait ESImplicits {

  implicit def richJsValue(value : JsValue) = new RichJsValue(value)
  implicit def richJsValue2(value : JsValue) = new RichJsValue2(value)
  implicit def richUpdateRequest(updateRequest : UpdateRequest) = new RichUpdateRequest(updateRequest)
  implicit def richEnity[A <: HasId[A]](entity : A) = new RichEntity[A](entity)

  implicit def stripVersionedFromFuture[T](f : Future[Versioned[T]])(implicit ec : ExecutionContext) = f.map(_.entity)
  implicit def stripVersionedFromFutureOption[T](f : Future[Option[Versioned[T]]])(implicit ec : ExecutionContext) = f.map(_.map(_.entity))
  implicit def stripVersionedFromFutureList[T](f : Future[List[Versioned[T]]])(implicit ec : ExecutionContext) = f.map(_.map(_.entity))
  implicit def convertToUnit(f : Future[_])(implicit ec : ExecutionContext) : Future[Unit] = f.map(_ => Unit)

}

