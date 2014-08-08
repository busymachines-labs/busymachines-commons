package com.busymachines.prefab.authentication

import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.prefab.authentication.db.{AuthenticationDao, CredentialsDao}
import com.busymachines.prefab.authentication.elasticsearch.{ESAuthenticationDao, ESCredentialsDao}
import com.busymachines.prefab.authentication.logic.AuthenticationConfig

import scala.concurrent.ExecutionContext

trait AuthenticationAssembly {

  implicit def executionContext: ExecutionContext
  def index: ESIndex
  def authenticationConfig: AuthenticationConfig

  lazy val credentialsDao: CredentialsDao = new ESCredentialsDao(index)
  lazy val authenticationDao: AuthenticationDao = new ESAuthenticationDao(index)

}
