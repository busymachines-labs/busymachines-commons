package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.implicits._
import scala.util.Random

object PasswordCredentials extends ((Id[PasswordCredentials], String, String) => PasswordCredentials) {
  def apply(password: String) = {
    val salt = Random.nextString(12)
    new PasswordCredentials(Id.generate, salt, (password + salt).md5.toHexString)
  }
}

case class PasswordCredentials(
    id:Id[PasswordCredentials],
    salt:String,
    passwordHash:String) extends HasId[PasswordCredentials] {
  
  def hasPassword(password: String) = 
    passwordHash == (password + salt).md5.toHexString
}