package com.busymachines.ghreports

import akka.actor.ActorSystem
import spray.routing.Directives._

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("ghreports")
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val ui = new UiServer
}