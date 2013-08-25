package com.busymachines.commons.http

import com.busymachines.commons.Logging
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.routing.Route
import akka.actor.ActorRefFactory

abstract class CommonHttpService(implicit val actorRefFactory : ActorRefFactory) extends HttpService with CommonDirectives with Route with Logging  {

  implicit def executionContext = actorRefFactory.dispatcher
  
  def apply(request: RequestContext) =
    route(request)

  def route : Route
}