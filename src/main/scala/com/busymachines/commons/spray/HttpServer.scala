package com.busymachines.commons.spray

import _root_.spray.http.HttpRequest
import com.busymachines.commons.{CommonConfig, NotAuthorizedException, EntityNotFoundException}
import com.busymachines.commons.logging.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import _root_.spray.can.Http
import _root_.spray.http.HttpRequest
import _root_.spray.http.StatusCodes
import _root_.spray.routing.Directive.pimpApply
import _root_.spray.routing.HttpService.pimpRouteWithConcatenation
import spray.routing._
import _root_.spray.routing.directives.LogEntry
import com.busymachines.commons.Implicits._
import _root_.spray.http.IllegalRequestException
import scala.util.control.NonFatal
import _root_.spray.util.LoggingContext
import _root_.spray.http._
import _root_.spray.json._
import StatusCodes._
import com.netaporter.salad.metrics.spray.metrics.{MetricHelpers, MetricsDirectiveFactory}
import com.netaporter.salad.metrics.spray.routing.directives.BasicDirectives._

case class HttpServerConfig(baseName: String) extends CommonConfig(baseName) {
  val interface = string("interface")
  val port = int("port")
}

abstract class HttpServer(config: HttpServerConfig,metricsDirectiveFactory: MetricsDirectiveFactory)(implicit actorSystem : ActorSystem) extends CommonHttpService with CORSDirectives{

  val serverName : String = "http-server"
  val routingSettings = RoutingSettings(actorSystem)

  private implicit def rs = routingSettings

  def route : Route

  class Actor extends HttpServiceActor {
    val theRoute = route

    def receive = runRoute(logRequest(showRequest _) {
      monitorRequest(null,theRoute) })

    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)
    def monitorRequest(ctx:RequestContext,route:Route):Route =
    {
      (metricsDirectiveFactory.counter("aurum").all.count &
        metricsDirectiveFactory.timer("aurum").time)(route)
    }
  }

  def start() =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = config.interface, port = config.port)

}
