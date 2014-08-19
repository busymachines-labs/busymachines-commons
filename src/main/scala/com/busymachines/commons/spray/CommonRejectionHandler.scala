package com.busymachines.commons.spray

import com.busymachines.commons.logger.Logging
import com.busymachines.commons.{NotAuthorizedException}
import spray.routing.{AuthenticationFailedRejection, Route, Rejection, RejectionHandler}

/**
 * Created by lorand on 02.07.2014.
 */
object CommonRejectionHandler extends RejectionHandler with Logging {
  override def isDefinedAt(x: List[Rejection]): Boolean = true

  override def apply(v1: List[Rejection]): Route = v1 match {
    case Nil => ctx => Unit // do nothing
    case e if e.exists(_.isInstanceOf[AuthenticationFailedRejection]) => ctx =>
      logger.debug(s"Processing rejection handler")
      logger.debug(s"Rejection is $e")
      throw new NotAuthorizedException(s"No valid Auth-Token present $e")
    case _ => ctx => {
      logger.debug(s"Processing rejection handler")
      logger.debug(s"Rejection is $v1")
      throw new Exception(s"${v1.mkString(",")}")
    }
  }
}
