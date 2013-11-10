package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id
import com.busymachines.commons.implicits._
import scala.util.Random

object PasswordCredentials extends ((String, String, String) => PasswordCredentials) {
  def apply(login : String, password: String) = {
    val salt = Random.nextString(12)
    new PasswordCredentials(login, salt, (password + salt).md5.toHexString)
  }
}

case class PasswordCredentials(
    login: String,
    salt: String,
    passwordHash:String) {
  
  def hasPassword(password: String) = 
    passwordHash == (password + salt).md5.toHexString
}