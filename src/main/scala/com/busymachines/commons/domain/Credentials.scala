package com.busymachines.commons.domain

import com.busymachines.commons.implicits._
import scala.util.Random

case class Credentials(
  id: Id[Credentials] = Id.generate,
  passwordCredentials: Option[PasswordCredentials] = None)

case class PasswordCredentials(
  email: String,
  salt: String = Random.nextString(8),
  passwordHash: Array[Byte] = Array.empty) {

  def withPassword(password: String) =
    this.copy(passwordHash = (salt + password).sha256Hash)
}
