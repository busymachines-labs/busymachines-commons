package com.busymachines.prefab.party

import com.busymachines.prefab.party.db.PartyDao
import com.busymachines.prefab.party.db.UserDao
import com.busymachines.prefab.authentication.elasticsearch.ESAuthenticationDao
import com.busymachines.prefab.party.service.PartyService
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.prefab.party.logic.PartyManager
import com.busymachines.prefab.party.logic.UserAuthenticator
import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import com.busymachines.prefab.party.api.v1.AuthenticationApi
import com.busymachines.prefab.party.api.v1.PartyApi
import com.busymachines.prefab.party.api.v1.UserApi
import akka.actor.ActorSystem

trait PartyAssembly {

  implicit def actorSystem : ActorSystem
  implicit def executionContext : ExecutionContext
  def index : ESIndex
  
  def partyIndex = index
  def authenticationIndex = index
  def authenticationConfigBaseName = "authentication"

  lazy val partyDao = new PartyDao(partyIndex)
  lazy val userDao = new UserDao(partyDao)
  lazy val credentialsDao = new ESCredentialsDao(partyIndex)
  lazy val authenticationDao = new ESAuthenticationDao(authenticationIndex)
  lazy val partyManager = new PartyManager(partyDao, userDao, credentialsDao, authenticator)
  lazy val authenticator = new UserAuthenticator(new AuthenticationConfig(authenticationConfigBaseName), partyDao, credentialsDao, authenticationDao)
  lazy val authenticationApiV1 = new AuthenticationApi(authenticator)
  lazy val userApiV1 = new UserApi(partyManager, authenticator)
  lazy val partyApiV1 = new PartyApi(partyManager, authenticator)


}