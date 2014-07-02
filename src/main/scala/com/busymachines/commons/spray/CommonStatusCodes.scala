package com.busymachines.commons.spray

import spray.http.StatusCodes

/**
 * @author Lorand Szakacs, lorand.szakacs@busymachines.com
 */
object CommonStatusCodes {
  val ApplicationExceptionStatus = StatusCodes.registerCustom(499, "Busy Machines Application Exception", "The application encountered a predictable exception")
}
