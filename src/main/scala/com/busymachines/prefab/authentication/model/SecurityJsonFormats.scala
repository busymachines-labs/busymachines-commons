package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.CommonJsonFormats

object SecurityJsonFormats extends SecurityJsonFormats

trait SecurityJsonFormats extends CommonJsonFormats {

  implicit val authenticationJsonFormat = jsonFormat3(Authentication)
  implicit val passwordCredentialsJsonFormat = jsonFormat3(PasswordCredentials)
  implicit val credentialJsonFormat = jsonFormat2(Credentials)
}
