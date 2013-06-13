package com.busymachines.commons.dao

class IdAlreadyExistsException(val id : String, val `type` : String) extends Exception(s"${`type`.capitalize} with id $id already exists")

class IdNotFoundException(val id : String, val `type` : String) extends Exception(s"${`type`.capitalize} with id $id not found")