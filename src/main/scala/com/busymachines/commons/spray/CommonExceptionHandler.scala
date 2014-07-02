package com.busymachines.commons.spray

import com.busymachines.commons.{ApplicationException, EntityNotFoundException, Logging, NotAuthorizedException}
import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, Route}
import spray.httpx.SprayJsonSupport._
import com.busymachines.commons.Implicits._

/**
 * @author Lorand Szakacs, lorand.szakacs@busymachines.com
 */
object CommonExceptionHandler extends ExceptionHandler with Logging {
  //since we handle Throwables it is safe to say that it is
  //defined for every exception, even though it is technically not true.
  override def isDefinedAt(x: Throwable): Boolean = true

  override def apply(v1: Throwable): Route = v1 match {
    case e: EntityNotFoundException => ctx =>
      ctx.complete(StatusCodes.NotFound, Map("message" -> e.getMessage, "id" -> e.id, "type" -> e.`type`))

    case e: NotAuthorizedException => ctx =>
      debug(s"Exception handler rejecting with forbidden because exception:\n${e.toString}")
      ctx.complete(StatusCodes.Forbidden)

    case e: ApplicationException => ctx =>
      debug(s"Application encountered a domain exception:\n${e.toString}")
      ctx.complete(CommonStatusCodes.ApplicationExceptionStatus, Map("message" -> e.message, "id" -> e.id))

    case e: Throwable => ctx =>
      error(s"Request ${ctx.request} could not be handled normally:\n${e.getMessage}", e)
      ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.getMessage))
  }
}
