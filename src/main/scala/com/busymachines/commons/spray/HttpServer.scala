package com.busymachines.commons.spray

import _root_.spray.can.Http
import _root_.spray.http.HttpRequest
import _root_.spray.routing.Directive.pimpApply
import _root_.spray.routing.directives.LogEntry
import akka.actor.{ActorSystem, Props}
import akka.event.Logging.DebugLevel
import akka.io.IO
import com.busymachines.commons.CommonConfig
import spray.routing._

case class HttpServerConfig(baseName: String) extends CommonConfig(baseName) {
  val interface = string("interface")
  val port = int("port")
}

abstract class HttpServer(config: HttpServerConfig)(implicit actorSystem: ActorSystem) extends CommonHttpService with CORSDirectives {

  val serverName: String = "http-server"
  val routingSettings = RoutingSettings(actorSystem)

  private implicit def rs = routingSettings

  def route: Route

  class Actor extends HttpServiceActor {
    val theRoute = route

    def receive = runRoute {
      logRequest(showRequest _) {
        theRoute
      }
    }

    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)
  }

  def start() =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = config.interface, port = config.port)

}
