package com.kentivo.mdm

import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfig
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESMediaDao
import com.busymachines.commons.http.UiService
import com.kentivo.mdm.api.ApiServer
import com.kentivo.mdm.api.v1.ApiDocV1
import com.kentivo.mdm.api.v1.AuthenticationApiV1
import com.kentivo.mdm.api.v1.PartiesApiV1
import com.kentivo.mdm.api.v1.SourceApiV1
import com.kentivo.mdm.api.v1.UsersApiV1
import com.kentivo.mdm.db.ItemDao
import com.kentivo.mdm.db.SourceDao
import com.kentivo.mdm.logic.SourceManager
import akka.actor.ActorSystem
import com.busymachines.commons.http.HttpServer
import com.kentivo.mdm.logic.PartyService
import com.kentivo.mdm.db.PartyDao
import com.busymachines.commons.event.DistributedEventBus
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.kentivo.mdm.db.UserDao
import com.kentivo.mdm.logic.UserAuthenticator
import com.kentivo.mdm.logic.UserAuthenticator
import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import com.busymachines.prefab.authentication.elasticsearch.ESAuthenticationDao
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("KentivoMDM")
  lazy implicit val executionContext = actorSystem.dispatcher

  lazy val eventBus = new DistributedEventBus(actorSystem)
  
  lazy val esConfig = new ESConfig("kentivo.es")
  lazy val esClient = new ESClient(esConfig)
  lazy val index = new ESIndex(esClient, "kentivo.mdm", eventBus) 

  lazy val partyDao = new PartyDao(index)
  lazy val userDao = new UserDao(partyDao)
  lazy val sourceDao = new SourceDao(index)
  lazy val itemDao = new ItemDao(index)
  lazy val mediaDao = new ESMediaDao(index)
  lazy val credentialsDao = new ESCredentialsDao(index)
  lazy val authenticationDao = new ESAuthenticationDao(index)
  lazy val partyService = new PartyService(partyDao, userDao, credentialsDao, authenticator)
  lazy val sourceManager = new SourceManager(sourceDao)
  lazy val authenticator = new UserAuthenticator(new AuthenticationConfig("kentivo.authentication"), partyDao, credentialsDao, authenticationDao)
  lazy val authenticationApiV1 = new AuthenticationApiV1(authenticator)
  lazy val userApiV1 = new UsersApiV1(partyService, authenticator)
  lazy val partyApiV1 = new PartiesApiV1(partyService, authenticator)
  lazy val sourceApiV1 = new SourceApiV1(sourceManager, authenticator)
  lazy val apiDocV1 = new ApiDocV1
  lazy val ui = new UiService
  lazy val server = new ApiServer(authenticationApiV1, partyApiV1, userApiV1, sourceApiV1, apiDocV1, ui)
}