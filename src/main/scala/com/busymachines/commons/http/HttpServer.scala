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
import spray.routing.HttpService

abstract class HttpServer(implicit actorSystem : ActorSystem) extends CommonHttpService with Logging {

  private implicit def eh = exceptionHandler
  private implicit def rs = routingSettings

  val interface : String = "localhost"
  val port : Int = 8080
  val exceptionHandler : ExceptionHandler = ExceptionHandler.default
  val routingSettings = RoutingSettings(actorSystem)

  def route : Route

  class Actor extends HttpServiceActor {
    val theRoute = route
    def receive = runRoute(logRequest(showRequest _) { theRoute })
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)

  }

  def start =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), "http-server"), interface = interface, port = port)
}
