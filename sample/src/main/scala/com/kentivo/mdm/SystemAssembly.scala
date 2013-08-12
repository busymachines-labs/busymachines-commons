package com.kentivo.mdm

import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESConfiguration
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.MediaDao
import com.busymachines.commons.http.UiService
import com.kentivo.mdm.api.ApiServer
import com.kentivo.mdm.api.UserAuthenticator
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
import com.kentivo.mdm.db.LoginDao

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("KentivoMDM")
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy implicit val authenticator = new UserAuthenticator

  val esConfig = new ESConfiguration
  val esClient = new ESClient(esConfig)
  val index = new ESIndex(esClient, "kentivo.mdm") 

  lazy val partyDao = new PartyDao(index)
  lazy val sourceDao = new SourceDao(index)
  lazy val itemDao = new ItemDao(index)
  lazy val mediaDao = new MediaDao(index)
  lazy val loginDao = new LoginDao(index)
  lazy val partyService = new PartyService(partyDao, loginDao)
  lazy val sourceManager = new SourceManager(sourceDao)
  lazy val authenticationApiV1 = new AuthenticationApiV1(authenticator)
  lazy val userApiV1 = new UsersApiV1(partyService, authenticator)
  lazy val partyApiV1 = new PartiesApiV1(partyService, authenticator)
  lazy val sourceApiV1 = new SourceApiV1(sourceManager, authenticator)
  lazy val apiDocV1 = new ApiDocV1
  lazy val ui = new UiService
  lazy val server = new ApiServer(authenticationApiV1, partyApiV1, userApiV1, sourceApiV1, apiDocV1, ui)
}