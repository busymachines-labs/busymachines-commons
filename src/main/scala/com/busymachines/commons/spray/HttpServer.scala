package com.busymachines.commons.spray

import com.busymachines.commons.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import spray.can.Http
import spray.http.HttpRequest
import spray.http.StatusCodes
import spray.routing.Directive.pimpApply
import spray.routing.HttpService.pimpRouteWithConcatenation
import spray.routing.HttpServiceActor
import spray.routing.Route
import spray.routing.RoutingSettings
import spray.routing.directives.LogEntry
import spray.routing.ExceptionHandler
import spray.routing.HttpService
import spray.routing.RejectionHandler
import spray.http.StatusCodes
import com.busymachines.commons.EntityNotFoundException
import com.busymachines.commons.NotAuthorizedException
import com.busymachines.commons.EntityNotFoundException
import com.busymachines.commons.domain.CommonJsonFormats._
import spray.http.IllegalRequestException
import scala.util.control.NonFatal
import spray.util.LoggingContext
import spray.http._
import spray.json._
import StatusCodes._
import spray.routing.AuthorizationFailedRejection

abstract class HttpServer(implicit actorSystem : ActorSystem) extends CommonHttpService with CORSDirectives with Logging {

  val interface : String = "localhost"
  val port : Int = 8080
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
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = interface, port = port)

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
