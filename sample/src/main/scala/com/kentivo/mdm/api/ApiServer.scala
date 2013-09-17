package com.kentivo.mdm.api

import com.busymachines.commons.Logging
import com.kentivo.mdm.api.v1.ApiDocV1
import com.kentivo.mdm.api.v1.AuthenticationApiV1
import com.kentivo.mdm.api.v1.PartiesApiV1
import com.kentivo.mdm.api.v1.SourceApiV1
import com.kentivo.mdm.api.v1.UsersApiV1
import com.kentivo.mdm.commons.InvalidIdException
import com.kentivo.mdm.commons.InvalidInputException
import com.kentivo.mdm.commons.NotAuthorizedException
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.DebugLevel
import akka.io.IO
import spray.can.Http
import spray.http.HttpRequest
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.json.DefaultJsonProtocol
import spray.routing.Directive.pimpApply
import spray.routing.Directives
import spray.routing.Directives._
import spray.routing.ExceptionHandler
import spray.routing.HttpServiceActor
import spray.routing.RequestContext
import spray.routing.RoutingSettings
import spray.routing.directives.LogEntry
import spray.routing.directives.LoggingMagnet.forMessageFromFullShow
import spray.util.LoggingContext
import com.busymachines.commons.http.HttpServer
import com.busymachines.commons.http.UiService
import com.kentivo.mdm.ui.UI

/**
 * Create a server for API project using spray-can.
 */
class ApiServer(authenticationApiV1: AuthenticationApiV1, partyApiV1: PartiesApiV1, userApiV1: UsersApiV1, sourceApiV1: SourceApiV1, apiDocV1: ApiDocV1, ui : UiService, leafsUi: UI)(implicit actorSystem: ActorSystem) extends HttpServer with DefaultJsonProtocol {

  override val route =
    pathPrefix("v1") {
      authenticationApiV1 ~
        partyApiV1 ~
        userApiV1 ~
        sourceApiV1 ~
        apiDocV1
    } ~ leafsUi.route

  override val exceptionHandler =
    ExceptionHandler {
      case e: NotAuthorizedException => ctx: RequestContext =>
        warn(s"Unauthorized request ${ctx.request} : ${e.message}", e)
        ctx.complete(StatusCodes.Unauthorized, "message" -> e.message)
        case e: InvalidIdException => ctx =>
        warn(s"Request ${ctx.request} contains invalid input: ${e.message}", e)
        ctx.complete(StatusCodes.BadRequest, Map("message" -> e.message, "id" -> e.id, "type" -> e.`type`))
        case e: InvalidInputException => ctx =>
        warn(s"Request ${ctx.request} contains invalid input: ${e.message}", e)
        ctx.complete(StatusCodes.BadRequest, Map("message" -> e.message, "field" -> e.field))
        case e: Throwable => ctx =>
          e.printStackTrace()
        error(s"Request ${ctx.request} could not be handled normally: ${e.getMessage}", e)
        ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.getMessage))
    }

}
