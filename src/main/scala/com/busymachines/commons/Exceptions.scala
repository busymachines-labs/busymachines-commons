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

