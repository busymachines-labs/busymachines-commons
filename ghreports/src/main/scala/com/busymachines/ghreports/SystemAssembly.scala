package com.busymachines.ghreports

import akka.actor.ActorSystem
import spray.routing.Directives._
import com.busymachines.commons.http.HttpServer
import com.busymachines.commons.http.UiService

class SystemAssembly {

  lazy implicit val actorSystem = ActorSystem("ghreports")
  lazy implicit val executionContext = actorSystem.dispatcher
  lazy val ui = new UiService()
  lazy val server = new HttpServer(ui)
}