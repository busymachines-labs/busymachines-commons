package com.busymachines.commons.http

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
import spray.routing.ExceptionHandler

class HttpServer(route : Route, interface : String = "localhost", port : Int = 8080)(implicit actorSystem: ActorSystem) extends Logging {

  implicit val eh = ExceptionHandler
  implicit val rs = RoutingSettings(actorSystem)

  def this(routes : Route*)(implicit actorSystem: ActorSystem) = 
    this(routes.reduce((r1, r2) => r1 ~ r2), "localhost", 8080)
  
  class Actor extends HttpServiceActor {
    def receive = runRoute(logRequest(showRequest _) {route})
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)

  }

  def start =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), "http-server"), interface = "localhost", port = 8080)
}
