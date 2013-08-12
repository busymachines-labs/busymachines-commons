package com.busymachines.commons.http

import spray.routing._
import spray.http.HttpHeader
import spray.http.StatusCodes.Forbidden
import spray.http.HttpHeaders.ModeledHeader
import spray.http.Rendering
import spray.http.Renderer
import spray.http.HttpHeaders

/**
 * Code copied from:
 * https://groups.google.com/forum/#!topic/spray-user/iVs5fn41LeY
 * and
 * https://gist.github.com/ayosec/4324747
 */

// See https://developer.mozilla.org/en-US/docs/HTTP/Access_control_CORS

trait CORSDirectives  { this: HttpService =>
  def respondWithCORSHeaders(origin: String) =
    respondWithHeaders(
      HttpHeaders.`Access-Control-Allow-Origin`(origin),
      HttpHeaders.`Access-Control-Allow-Credentials`(true))

  def corsFilter(origin: String)(route: Route) =
    if (origin == "*")
      respondWithCORSHeaders("*")(route)
    else
      optionalHeaderValueByName("Origin") {
        case None => route
        case Some(clientOrigin) =>
          if (origin == clientOrigin)
            respondWithCORSHeaders(origin)(route)
          else
            complete(Forbidden, Nil, "Invalid origin") // Maybe, a Rejection will fit better
      }
}