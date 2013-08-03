package com.busymachines.ghreports

import akka.actor.ActorSystem
import spray.routing.Directives._
import com.busymachines.commons.spray.HttpServer
import com.busymachines.commons.spray.StandardUiService

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("ghreports")
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val ui = new StandardUiService
  lazy val server = new HttpServer(ui)
}