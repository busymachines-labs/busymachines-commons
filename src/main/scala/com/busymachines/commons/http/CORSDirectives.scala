package com.busymachines.commons.http

import spray.routing._
import spray.http.HttpHeader
import spray.http.StatusCodes.Forbidden
import spray.http.HttpHeaders.ModeledHeader
import spray.http.Rendering
import spray.http.Renderer
import spray.http.HttpHeaders
import spray.http.HttpMethods
import spray.http.ContentTypes
import spray.http.AllowedOrigins
import spray.http.AllOrigins
import spray.http.SomeOrigins

/**
 * Code copied from:
 * https://groups.google.com/forum/#!topic/spray-user/iVs5fn41LeY
 * and
 * https://gist.github.com/ayosec/4324747
 */

// See https://developer.mozilla.org/en-US/docs/HTTP/Access_control_CORS

trait CORSDirectives { this: HttpService =>
  def respondWithCORSHeaders(origin: AllowedOrigins) =
   respondWithHeaders(
    HttpHeaders.`Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.DELETE, HttpMethods.OPTIONS, HttpMethods.PUT),
    HttpHeaders.`Access-Control-Allow-Headers`("X-Requested-With, Cache-Control, Pragma, Origin, Authorization, Content-Type, Auth-Token"),
    HttpHeaders.`Access-Control-Expose-Headers`("Auth-Token"),
    HttpHeaders.`Access-Control-Allow-Origin`(origin),
    HttpHeaders.`Access-Control-Allow-Credentials`(true))
    
  def corsFilter(origin: String)(route: Route) =
    if (origin == "*")
      respondWithCORSHeaders(AllOrigins)(route)
    else
      optionalHeaderValueByName("Origin") {
        case None => route
        case Some(clientOrigin) =>
          if (origin == clientOrigin)
            respondWithCORSHeaders(SomeOrigins(origin :: Nil))(route)
          else
            complete(Forbidden, Nil, "Invalid origin") // Maybe, a Rejection will fit better
      }


  def crossDomain = respondWithCORSHeaders(AllOrigins)
}