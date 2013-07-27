package com.kentivo.mdm

import com.kentivo.mdm.api.Api
import com.kentivo.mdm.api.ApiServer
import com.kentivo.mdm.api.v1.ApiDocV1
import com.kentivo.mdm.api.v1.AuthenticationApiV1
import com.kentivo.mdm.api.v1.PartiesApiV1
import com.kentivo.mdm.api.v1.SourceApiV1
import com.kentivo.mdm.api.v1.UsersApiV1
import com.kentivo.mdm.db.SourceDao
import com.kentivo.mdm.logic.SourceManager
import com.kentivo.mdm.ui.Ui
import akka.actor.ActorSystem
import spray.routing.Directives._
import com.kentivo.mdm.db.ItemDao
import com.busymachines.commons.elasticsearch.MediaDao
import com.kentivo.mdm.db.MdmIndex
import com.busymachines.commons.concurrent.SimpleExecutionContext
import com.busymachines.commons.elasticsearch.ESConfiguration
import com.busymachines.commons.elasticsearch.ESClient
import com.busymachines.commons.elasticsearch.ESIndex

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("KentivoMDM")
  lazy implicit val executionContext = actorSystem.dispatcher

  val esConfig = new ESConfiguration
  val esClient = new ESClient(esConfig)
  val index = new ESIndex(esClient, "kentivo.mdm") 

  lazy val sourceDao = new SourceDao(index)
  lazy val itemDao = new ItemDao(index)
  lazy val mediaDao = new MediaDao(index)
  lazy val sourceManager = new SourceManager(sourceDao)
  lazy val authenticationApiV1 = new AuthenticationApiV1
  lazy val userApiV1 = new UsersApiV1
  lazy val partyApiV1 = new PartiesApiV1
  lazy val sourceApiV1 = new SourceApiV1(sourceManager)
  lazy val apiDocV1 = new ApiDocV1
  lazy val api = new ApiServer(actorSystem, authenticationApiV1, partyApiV1, userApiV1, sourceApiV1, apiDocV1)
  lazy val ui = new Ui
  api.route ~ ui.route
}