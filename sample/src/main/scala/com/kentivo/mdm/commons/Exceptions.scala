package com.kentivo.mdm.commons

class pdmException(message : String, cause : Throwable = null) extends Exception(message, cause) {
  def message = getMessage
}

class InvalidInputException(message : String, val field: String = "", cause : Throwable = null) extends pdmException(message, cause) 

/**
 * Thrown when the logged in user is not authorized. The message should be descriptive, in English and is meant for end-user
 * consumption.
 */
class NotAuthorizedException(message : String, cause : Throwable = null) extends pdmException(message, cause)

/**
 * Thrown when a specified id was not valid. The message should be descriptive, contain the , english and is meant for end-user
 * consumption.
 */
class InvalidIdException(val id : String, val `type` : String) extends InvalidInputException(s"${`type`.capitalize} with id $id not found")

/**
 * Thrown when a specified id was not valid. The message should be descriptive, contain the , english and is meant for end-user
 * consumption.
 */
class InvalidConfigurationException(val key : String, val value : String) extends pdmException(s"Invalid configuration value for key $key: $value")
