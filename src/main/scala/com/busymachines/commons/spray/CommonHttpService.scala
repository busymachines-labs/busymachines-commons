package com.busymachines.commons.spray

import scala.concurrent.duration.FiniteDuration
import com.busymachines.commons.Logging
import akka.actor.ActorRefFactory
import spray.http.CacheDirectives
import spray.http.HttpHeaders
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.routing.Route

abstract class CommonHttpService(implicit val actorRefFactory: ActorRefFactory) extends HttpService with CommonDirectives with Route with Logging {
  implicit def executionContext = actorRefFactory.dispatcher

  def apply(request: RequestContext) =
    route(request)

  def route: Route

  def cacheHeaders(duration: FiniteDuration) =
    respondWithHeaders(HttpHeaders.`Cache-Control`(CacheDirectives.`private`()), HttpHeaders.`Access-Control-Max-Age`(duration.toSeconds))
}