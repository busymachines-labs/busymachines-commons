package com.kentivo.mdm.api

import com.kentivo.mdm.api.v1.ApiDocV1
import com.kentivo.mdm.api.v1.AuthenticationApiV1
import com.kentivo.mdm.api.v1.PartiesApiV1
import com.kentivo.mdm.api.v1.UsersApiV1
import com.kentivo.mdm.commons.InvalidIdException
import com.kentivo.mdm.commons.InvalidInputException
import com.kentivo.mdm.commons.NotAuthorizedException
import akka.event.Logging.DebugLevel
import spray.http.HttpRequest
import spray.http.StatusCodes
import spray.routing.Directive.pimpApply
import spray.routing.ExceptionHandler
import spray.routing.directives.LogEntry
import spray.routing.directives.LoggingMagnet.forMessageFromFullShow
import akka.actor.ActorSystem
import akka.actor.ActorContext
import com.kentivo.mdm.api.v1.SourceApiV1
import akka.actor.ActorRefFactory

class WithContext(val context: ActorContext)

class Api(authenticationApiV1: AuthenticationApiV1, partyApiV1: PartiesApiV1, userApiV1: UsersApiV1, sourceApiV1: SourceApiV1, apiDocV1: ApiDocV1)(implicit val actorRefFactory: ActorRefFactory) extends ApiDirectives {

  val route =
    pathPrefix("v1") {
      logRequest(showRequest _) {
        authenticationApiV1.route ~
          partyApiV1.route ~
          userApiV1.route ~
          sourceApiV1.route ~
          apiDocV1.route
      }
    }

  def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, DebugLevel)

  implicit val exceptionHandler = ExceptionHandler.fromPF {
    case e: NotAuthorizedException => ctx =>
      warn(s"Unauthorized request ${ctx.request} : ${e.message}", e)
      ctx.complete(StatusCodes.Unauthorized, Map("message" -> e.message))
      case e: InvalidIdException => ctx =>
      warn(s"Request ${ctx.request} contains invalid input: ${e.message}", e)
      ctx.complete(StatusCodes.BadRequest, Map("message" -> e.message, "id" -> e.id, "type" -> e.`type`))
      case e: InvalidInputException => ctx =>
      warn(s"Request ${ctx.request} contains invalid input: ${e.message}", e)
      ctx.complete(StatusCodes.BadRequest, Map("message" -> e.message, "field" -> e.field))
      case e: Throwable => ctx =>
      error(s"Request ${ctx.request} could not be handled normally: ${e.getMessage}", e)
      ctx.complete(StatusCodes.InternalServerError, Map("message" -> e.getMessage))
  }
}

