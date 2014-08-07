package com.busymachines.commons

import com.busymachines.commons.domain.Id

/**
 * An exception that holds an id and parameters that can be used to serialize exceptions and to
 * construct translated messages.
 */
class CommonException(val message: String, _errorId: Option[String] = None, val parameters: Map[String, String], val cause: Option[Throwable])
  extends Exception(message, cause.orNull) {

  def this (message: String, errorId: String, parameters: (String, String)*) =
    this(message, Some(errorId), parameters.toMap, None)

  def this (message: String, parameters: (String, String)*) =
    this(message, None, parameters.toMap, None)

  def errorId =
    _errorId.getOrElse(getClass.getSimpleName).stripSuffix("Exception")

  override def toString =
    s"$errorId:$message"
}

class NotAuthenticatedException(message: String, errorId: Option[String] = None, parameters: Map[String, String] = Map.empty, cause: Option[Throwable] = None)
  extends CommonException(message, errorId, parameters, cause) {

  def this (message: String, errorId: String, parameters: (String, String)*) =
    this(message, Some(errorId), parameters.toMap, None)

  def this (message: String, parameters: (String, String)*) =
    this(message, None, parameters.toMap, None)
}

class NotAuthorizedException(message: String, errorId: Option[String] = None, parameters: Map[String, String] = Map.empty, cause: Option[Throwable] = None)
  extends CommonException(message, errorId, parameters, cause) {

  def this (message: String, errorId: String, parameters: (String, String)*) =
    this(message, Some(errorId), parameters.toMap, None)

  def this (message: String, parameters: (String, String)*) =
    this(message, None, parameters.toMap, None)
}

/**
 * Thrown when a specified id was not valid. The message should be descriptive, contain the , english and is meant for end-user
 * consumption.
 */
class EntityNotFoundException(message: String, errorId: Option[String] = None, parameters: Map[String, String] = Map.empty, cause: Option[Throwable] = None)
  extends CommonException(message, errorId, parameters, cause) {

  def this(id: String, `type`: String) =
    this(s"${`type`.capitalize} with id '$id' not found", parameters = Map("type" -> `type`, "id" -> id))

  def this(id: Id[_], `type`: String) =
    this(id.value, `type`)

  def this (message: String, errorId: String, parameters: (String, String)*) =
    this(message, Some(errorId), parameters.toMap, None)

  def this (message: String, parameters: (String, String)*) =
    this(message, None, parameters.toMap, None)
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
class ApplicationException(message: String, errorId: Option[String] = None, parameters: Map[String, String] = Map.empty, cause: Option[Throwable] = None)
  extends CommonException(message, errorId, parameters, cause) {

  def this (message: String, errorId: String, parameters: (String, String)*) =
    this(message, Some(errorId), parameters.toMap, None)

  def this (message: String, parameters: (String, String)*) =
    this(message, None, parameters.toMap, None)
}


