/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
package object rest {

  import akka.http.scaladsl
  import akka.http.scaladsl.settings
  import akka.http.scaladsl.server
  import akka.http.scaladsl.model

  val Http: scaladsl.Http.type = scaladsl.Http

  type HttpRequest = model.HttpRequest
  val HttpRequest: model.HttpRequest.type = model.HttpRequest

  type HttpResponse = model.HttpResponse
  val HttpResponse: model.HttpResponse.type = model.HttpResponse

  type HttpEntity = model.HttpEntity
  val HttpEntity: model.HttpEntity.type = model.HttpEntity

  type Route = server.Route
  val Route: server.Route.type = server.Route

  type Directives = server.Directives
  val Directives: server.Directives.type = server.Directives

  type ExceptionHandler = server.ExceptionHandler
  val ExceptionHandler: server.ExceptionHandler.type = server.ExceptionHandler

  type RejectionHandler = server.RejectionHandler
  val RejectionHandler: server.RejectionHandler.type = server.RejectionHandler

  type StatusCode = model.StatusCode
  val StatusCode:  model.StatusCode.type  = model.StatusCode
  val StatusCodes: model.StatusCodes.type = model.StatusCodes

  type ContentType = model.ContentType
  val ContentType:  model.ContentType.type  = model.ContentType
  val ContentTypes: model.ContentTypes.type = model.ContentTypes

  type RoutingSettings = settings.RoutingSettings
  val RoutingSettings: settings.RoutingSettings.type = settings.RoutingSettings

  type ParserSettings = akka.http.scaladsl.settings.ParserSettings
  val ParserSettings: akka.http.scaladsl.settings.ParserSettings.type = akka.http.scaladsl.settings.ParserSettings
}
