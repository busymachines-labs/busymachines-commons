package com.busymachines.commons.spray

import com.busymachines.commons.{NotAuthorizedException, Logging}
import spray.routing.{AuthenticationFailedRejection, Route, Rejection, RejectionHandler}

/**
 * Created by lorand on 02.07.2014.
 */
object CommonRejectionHandler extends RejectionHandler with Logging{
  override def isDefinedAt(x: List[Rejection]): Boolean = true

  override def apply(v1: List[Rejection]): Route = v1 match {
    case e: List[Rejection] => ctx => {
      debug(s"Processing rejection handler")
      debug(s"Rejection is $e")

      e.find(_.isInstanceOf[AuthenticationFailedRejection]) match {
        case Some(_) => throw new NotAuthorizedException(s"No valid Auth-Token present $e")
          //TODO: iterate over rejections and figure out appropriate ApplicationExceptions to throw
        case _ => throw new Exception(s"$e")
      }

    }
  }
}
