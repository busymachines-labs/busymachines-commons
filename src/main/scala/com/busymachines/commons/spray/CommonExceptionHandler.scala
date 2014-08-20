package com.busymachines.commons.spray

import com.busymachines.commons._
import _root_.spray.http.StatusCodes
import _root_.spray.routing.{ExceptionHandler, Route}
import _root_.spray.httpx.SprayJsonSupport._
import com.busymachines.commons.Implicits._
import com.busymachines.commons.logging.Logging

/**
 * @author Lorand Szakacs, lorand.szakacs@busymachines.com
 */
object CommonExceptionHandler extends ExceptionHandler with Logging {
  //since we handle Throwables it is safe to say that it is
  //defined for every exception, even though it is technically not true.
  override def isDefinedAt(x: Throwable): Boolean = true

  override def apply(v1: Throwable): Route = v1 match {
    case e: EntityNotFoundException => ctx =>
      ctx.complete(StatusCodes.NotFound, Map("message" -> e.message, "error" -> e.errorId) ++ e.parameters)

    case e: NotAuthorizedException => ctx =>
      logger.debug(s"Exception handler rejecting with forbidden because exception:\n${e.toString}")
      ctx.complete(StatusCodes.Forbidden, Map("message" -> e.message, "error" -> e.errorId) ++ e.parameters)

    case e: ApplicationException => ctx =>
      logger.debug(s"Request ${ctx.request} could not be handled normally:\n${e.message}", e)
      ctx.complete(CommonStatusCodes.ApplicationExceptionStatus, Map("message" -> e.message, "error" -> e.errorId) ++ e.parameters)

    case e: CommonException => ctx =>
      logger.error(s"Request ${ctx.request} could not be handled normally:\n${e.message}", e)
      ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.message, "error" -> e.errorId) ++ e.parameters)

    case e: Throwable => ctx =>
      logger.error(s"Request ${ctx.request} could not be handled normally:\n${e.getMessage}", e)
      ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.getMessage))
  }
}
