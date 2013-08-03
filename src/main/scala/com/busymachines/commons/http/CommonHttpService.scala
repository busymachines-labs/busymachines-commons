package com.busymachines.commons.http

import com.busymachines.commons.Logging
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.routing.Route

trait CommonHttpService extends HttpService with Route with Logging {

  implicit def executionContext = actorRefFactory.dispatcher
  
  def apply(request: RequestContext) =
    route(request)

  val route : Route
}