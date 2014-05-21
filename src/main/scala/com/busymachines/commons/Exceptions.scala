package com.busymachines.commons

import com.busymachines.commons.domain.Id

/**
 * Authentication is missing or invalid.
 *
 * @param msg
 * @param cause
 */
class AuthenticationException(msg:String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))

/**
 * Server refuses to fulfill the request.
 *
 * Authorization will not help and request SHOULD NOT be repeated.
 *
 * @param msg
 * @param cause
 */
class ForbiddenException(msg:String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))

/**
 * No entity found matching provided identifier.
 *
 * Can be given in place of ForbiddenException if server does not want to reveal the reason for rejecting the request.
 */
class EntityNotFoundException(val id : String, val `type` : String) extends Exception(s"${`type`.capitalize} with id $id not found") {
  def this(id : Id[_], `type` : String) = this(id.toString, `type`)
}