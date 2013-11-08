package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials

object CredentialsMapping extends ESMapping[Credentials] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val passwordCredentials = "passwordCredentials" as Nested(PasswordCredentialsMapping)
}

object PasswordCredentialsMapping extends ESMapping[PasswordCredentials] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val login = "login" as String & NotAnalyzed
  val salt = "salt" as String & NotAnalyzed
  val passwordHash = "passwordHash" as String & NotAnalyzed
}
