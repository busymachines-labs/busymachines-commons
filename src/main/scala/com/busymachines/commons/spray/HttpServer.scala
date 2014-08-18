package com.busymachines.commons.spray

import _root_.spray.http.HttpRequest
import com.busymachines.commons.{CommonConfig, NotAuthorizedException, EntityNotFoundException}
import com.busymachines.commons.logger.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import _root_.spray.can.Http
import _root_.spray.http.HttpRequest
import _root_.spray.http.StatusCodes
import _root_.spray.routing.Directive.pimpApply
import _root_.spray.routing.HttpService.pimpRouteWithConcatenation
import _root_.spray.routing.HttpServiceActor
import _root_.spray.routing.Route
import _root_.spray.routing.RoutingSettings
import _root_.spray.routing.directives.LogEntry
import _root_.spray.routing.ExceptionHandler
import _root_.spray.routing.HttpService
import _root_.spray.routing.RejectionHandler
import com.busymachines.commons.Implicits._
import _root_.spray.http.IllegalRequestException
import scala.util.control.NonFatal
import _root_.spray.util.LoggingContext
import _root_.spray.http._
import _root_.spray.json._
import StatusCodes._
import _root_.spray.routing.AuthorizationFailedRejection

case class HttpServerConfig(baseName: String) extends CommonConfig(baseName) {
  val interface = string("interface")
  val port = int("port")
}

abstract class HttpServer(config: HttpServerConfig)(implicit actorSystem : ActorSystem) extends CommonHttpService with CORSDirectives{

  val serverName : String = "http-server"
  val routingSettings = RoutingSettings(actorSystem)

  private implicit def rs = routingSettings

  def route : Route

  class Actor extends HttpServiceActor {
    val theRoute = route
    def receive = runRoute(logRequest(showRequest _) { theRoute })
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)
  }

  def start() =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = config.interface, port = config.port)

}
