package com.busymachines.prefab.authentication

import com.busymachines.commons.implicits._
import com.busymachines.prefab.authentication.model._

package object implicits {
  implicit val authenticationJsonFormat = format3(Authentication)
  implicit val passwordCredentialsJsonFormat = format3(PasswordCredentials)
  implicit val credentialJsonFormat = format2(Credentials)

}