package com.busymachines.commons

import com.busymachines.commons.domain.Id

class NotAuthenticatedException(msg: String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))

class NotAuthorizedException(msg: String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))

/**
 * Thrown when a specified id was not valid. The message should be descriptive, contain the , english and is meant for end-user
 * consumption.
 */
class EntityNotFoundException(val id: String, val `type`: String) extends Exception(s"${`type`.capitalize} with id $id not found") {
  def this(id: Id[_], `type`: String) = this(id.toString, `type`)
}
/**
 * This exception should be used when known, application specific events occur. Any client of the library should
 * define its different exceptions by using a fixed set of possible values for the id parameter.
 *
 * The {@link com.busymachines.commons.spray.CommonExceptionHandler} returns by default the 499 http status code
 * for this exception, regardless of the fields.
 *
 * @author Lorand Szakacs, lorand.szakacs@busymachines.com
 */
class ApplicationException(val id: String, msg: String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null)) {
  lazy val message = getMessage

  override def toString = s"id=$id\n${super.toString}"

}

