package com.busymachines.commons

class NotAuthenticatedException(msg : String, cause : Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))
class NotAuthorizedException(msg:String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))
class EntityNotFoundException(msg:String, cause: Option[Throwable] = None) extends Exception(msg, cause.getOrElse(null))
