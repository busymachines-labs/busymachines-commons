package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.busymachines.prefab.authentication.model.SecurityJsonFormats._
import com.busymachines.commons.domain.Id

object CredentialsMapping extends ESMapping[Credentials] {
  val id = "_id" -> "id" :: String.as[Id[Credentials]] & NotAnalyzed
  val passwordCredentials = "passwordCredentials" :: Nested(PasswordCredentialsMapping)
}

object PasswordCredentialsMapping extends ESMapping[PasswordCredentials] {
  val login = "login" :: String & NotAnalyzed
  val salt = "salt" :: String & NotAnalyzed
  val passwordHash = "passwordHash" :: String & NotAnalyzed
}
