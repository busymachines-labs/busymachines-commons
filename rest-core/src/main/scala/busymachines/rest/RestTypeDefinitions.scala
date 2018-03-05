package busymachines.rest

import akka.http.scaladsl
import akka.http.scaladsl.settings
import akka.http.scaladsl.server
import akka.http.scaladsl.model

/**
  *
  * These definitions are not
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Feb 2018
  *
  */
trait RestTypeDefinitions {

  @inline final val Http: scaladsl.Http.type = scaladsl.Http

  final type HttpRequest = model.HttpRequest
  @inline final val HttpRequest: model.HttpRequest.type = model.HttpRequest

  final type HttpResponse = model.HttpResponse
  @inline final val HttpResponse: model.HttpResponse.type = model.HttpResponse

  final type HttpEntity = model.HttpEntity
  @inline final val HttpEntity: model.HttpEntity.type = model.HttpEntity

  final type Route = server.Route
  @inline final val Route: server.Route.type = server.Route

  final type Directives = server.Directives
  @inline final val Directives: server.Directives.type = server.Directives

  final type ExceptionHandler = server.ExceptionHandler
  @inline final val ExceptionHandler: server.ExceptionHandler.type = server.ExceptionHandler

  final type RejectionHandler = server.RejectionHandler
  @inline final val RejectionHandler: server.RejectionHandler.type = server.RejectionHandler

  final type StatusCode = model.StatusCode
  @inline final val StatusCode:  model.StatusCode.type  = model.StatusCode
  @inline final val StatusCodes: model.StatusCodes.type = model.StatusCodes

  final type ContentType = model.ContentType
  @inline final val ContentType:  model.ContentType.type  = model.ContentType
  @inline final val ContentTypes: model.ContentTypes.type = model.ContentTypes

  final type RoutingSettings = settings.RoutingSettings
  @inline final val RoutingSettings: settings.RoutingSettings.type = settings.RoutingSettings

  final type ParserSettings = akka.http.scaladsl.settings.ParserSettings

  @inline final val ParserSettings: akka.http.scaladsl.settings.ParserSettings.type =
    akka.http.scaladsl.settings.ParserSettings

}
