package com.busymachines.commons.spray

import _root_.spray.http.HttpRequest
import com.busymachines.commons.CommonConfig
import com.busymachines.commons.Logging
import com.busymachines.commons.NotAuthorizedException
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
import com.busymachines.commons.EntityNotFoundException

case class HttpServerConfig(baseName: String) extends CommonConfig(baseName) {
  val interface = string("interface")
  val port = int("port")
}

abstract class HttpServer(config: HttpServerConfig)(implicit actorSystem : ActorSystem) extends CommonHttpService with CORSDirectives with Logging {

  val serverName : String = "http-server"
  val exceptionHandler : ExceptionHandler = commonExceptionHandler
  def rejectionHandler : RejectionHandler = commonRejectionHandler
  val routingSettings = RoutingSettings(actorSystem)

  private implicit def eh = exceptionHandler
  private implicit val rh = rejectionHandler 
  private implicit def rs = routingSettings

  def route : Route

  class Actor extends HttpServiceActor {
    val theRoute = route
    def receive = runRoute(logRequest(showRequest _) { theRoute })
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)
  }

  def start() =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = config.interface, port = config.port)

  def commonRejectionHandler = RejectionHandler {
    case rejections if rejections.nonEmpty => 
      crossDomain { 
        RejectionHandler.Default(rejections)
      }
  }
    
  def commonExceptionHandler = ExceptionHandler {
     case e: NotAuthorizedException => crossDomain {
      debug(s"Exception handler rejecting with forbidden because exception : $e")
      complete(StatusCodes.Unauthorized)
    }
    case e: EntityNotFoundException => crossDomain { ctx =>
      ctx.complete(StatusCodes.NotFound, Map("message" -> e.getMessage, "id" -> e.id, "type" -> e.`type`).toJson.toString)
    }
    case e: IllegalRequestException => crossDomain { ctx =>
      warn("Illegal request {}\n\t{}\n\tCompleting with '{}' response",
      ctx.request, e.getMessage, e.status)
      ctx.complete(e.status, e.info.format(routingSettings.verboseErrorMessages))
    }
    case e: RequestProcessingException => crossDomain { ctx =>
      warn("Request {} could not be handled normally\n\t{}\n\tCompleting with '{}' response",
      ctx.request, e.getMessage, e.status)
      ctx.complete(e.status, e.info.format(routingSettings.verboseErrorMessages))
    }
    case NonFatal(e) => crossDomain { ctx =>
      error(e, "Error during processing of request {}", ctx.request)
      ctx.complete(InternalServerError)
    } 
    case e: Throwable => crossDomain { ctx =>
      error(s"Request ${ctx.request} could not be handled normally: ${e.getMessage}", e)
      ctx.complete(StatusCodes.InternalServerError, e.getMessage)
    } 
  } 
}
