package com.busymachines.commons.test

import akka.actor.ActorSystem
import com.busymachines.commons.event.LocalEventBus
import com.busymachines.commons.implicits._
import com.busymachines.commons.testing.EmptyESTestIndex
import com.busymachines.prefab.party.PartyAssembly
import com.busymachines.prefab.party.logic.PartyFixture
import org.scalatest.FlatSpec
import spray.testkit.{RouteTest, ScalatestRouteTest}


trait AssemblyTestBase extends FlatSpec with PartyAssembly with RouteTest with ScalatestRouteTest {

  // system setup
  lazy implicit val actorSystem = ActorSystem("Commons")
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val eventBus = new LocalEventBus(actorSystem)
  lazy val index = new EmptyESTestIndex(getClass, eventBus)
  def actorRefFactory = actorSystem

  PartyFixture.createDevMode(partyDao, credentialsDao)

  // put the resulting security context in an implicit val
  def login(userName : String, password : String) =
    userAuthenticator.authenticateWithLoginNamePassword(userName, password).await.get
}