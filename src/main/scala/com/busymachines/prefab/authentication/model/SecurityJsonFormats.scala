package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.CommonJsonFormats

object SecurityJsonFormats extends SecurityJsonFormats

trait SecurityJsonFormats extends CommonJsonFormats {

  implicit val authenticationJsonFormat = format3(Authentication)
  implicit val passwordCredentialsJsonFormat = format3(PasswordCredentials)
  implicit val credentialJsonFormat = format2(Credentials)
}
