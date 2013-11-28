package com.busymachines.commons.spray

import spray.routing.HttpService
import com.busymachines.commons.Logging
import spray.routing.RequestContext
import spray.routing.Route
import akka.actor.ActorRefFactory
import scala.concurrent.duration.FiniteDuration
import spray.http.HttpHeaders
import spray.http.CacheDirectives
import org.apache.commons.codec.binary.Base64

abstract class CommonHttpService(implicit val actorRefFactory: ActorRefFactory) extends HttpService with CommonDirectives with Route with Logging {
  implicit def executionContext = actorRefFactory.dispatcher

  def apply(request: RequestContext) =
    route(request)

  def route: Route

  def cacheHeaders(duration: FiniteDuration) =
    respondWithHeaders(HttpHeaders.`Cache-Control`(CacheDirectives.`private`()), HttpHeaders.`Access-Control-Max-Age`(duration.toSeconds))

  def decodeBase64(src: String): Option[Array[Byte]] = {
    val base64 = "data:(.*);base64,(.*)".r
    src match {
      case base64(mimetype, data) => Some(Base64.decodeBase64(data.getBytes("UTF-8")))
      case _ => {
        None
      }
    }
  }

}