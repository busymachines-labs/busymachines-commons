package com.busymachines.commons.spray

import com.busymachines.commons.Logging

import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import spray.can.Http
import spray.http.HttpRequest
import spray.routing.Directive.pimpApply
import spray.routing.HttpService.pimpRouteWithConcatenation
import spray.routing.HttpServiceActor
import spray.routing.Route
import spray.routing.RoutingSettings
import spray.routing.directives.LogEntry

class HttpServer(routes : Route*)(implicit actorSystem: ActorSystem) extends Logging {
  
  val interface = "localhost"
  val port = 8080
  
  val route = routes.reduce((route1, route2) => route1 ~ route2)
  
  class Actor extends HttpServiceActor {
    def receive = runRoute(logRequest(showRequest _) {route})
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)

  }

  implicit val rs = RoutingSettings(actorSystem)

  def start =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), "http-server"), interface = "localhost", port = 8080)
}
