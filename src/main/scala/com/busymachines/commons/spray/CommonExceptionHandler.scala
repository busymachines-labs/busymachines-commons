package com.busymachines.commons.spray

import com.busymachines.commons.Logging
import spray.http.StatusCodes
import spray.routing.{Route, ExceptionHandler}

import spray.httpx.SprayJsonSupport._
import com.busymachines.commons.Implicits._

/**
 * Created by lorand.szakacs@busymachines.com on 02.07.2014.
 */
object CommonExceptionHandler extends ExceptionHandler with Logging {
  //since we handle Throwables it is safe to say that it is
  //defined for every exception, even though it is technically not true.
  override def isDefinedAt(x: Throwable): Boolean = true

  override def apply(v1: Throwable): Route = v1 match {
    case e: Throwable => ctx =>
      error(s"Request ${ctx.request} could not be handled normally: ${e.getMessage}", e)
      ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.getMessage))
  }
}
