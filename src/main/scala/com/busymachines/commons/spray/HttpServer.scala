package com.busymachines.commons.spray

import com.busymachines.commons._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import _root_.spray.can.Http
import _root_.spray.routing.Directive.pimpApply
import _root_.spray.routing.HttpServiceActor
import _root_.spray.routing.Route
import _root_.spray.routing.RoutingSettings
import _root_.spray.routing.directives.LogEntry
import _root_.spray.routing.ExceptionHandler
import _root_.spray.routing.RejectionHandler
import com.busymachines.commons.domain.CommonJsonFormats._
import scala.util.control.NonFatal
import _root_.spray.http._
import _root_.spray.json._
import StatusCodes._

case class HttpServerConfig(baseName: String) extends CommonConfig(baseName) {
  val interface = string("interface")
  val port = int("port")
}

abstract class HttpServer(config: HttpServerConfig)(implicit actorSystem: ActorSystem) extends CommonHttpService with CORSDirectives with Logging {

  val serverName: String = "http-server"

  def route: Route

  def start() =
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), serverName), interface = config.interface, port = config.port)

  private implicit val routingSettings = RoutingSettings(actorSystem)

  implicit val commonRejectionHandler = RejectionHandler {
    case rejections if rejections.nonEmpty =>
      crossDomain {
        RejectionHandler.Default(rejections)
      }
  }

  implicit val commonExceptionHandler = ExceptionHandler {
    case e: AuthenticationException => crossDomain { ctx =>
      debug(s"Request cannot be processed: authentication required.\n $e")
      ctx.complete(StatusCodes.Unauthorized)
    }
    case e: ForbiddenException => crossDomain { ctx =>
      debug(s"Request cannot be processed: access denied.\n $e")
      ctx.complete(StatusCodes.Forbidden)
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

  class Actor extends HttpServiceActor {
    def receive = runRoute(logRequest(showRequest _) {
      route
    })

    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)
  }

}
