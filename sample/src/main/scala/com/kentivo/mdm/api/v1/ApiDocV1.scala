package com.kentivo.mdm.api.v1

import com.kentivo.mdm.api.ApiDirectives

import akka.actor.ActorRefFactory

/**
 * Define routes for swagger documentation.
 */
class ApiDocV1(implicit val actorRefFactory: ActorRefFactory) extends ApiDirectives {
  val route = {
    path("api.html" / Rest) { path =>
      getFromResource("api/public/html/restApiDoc.html")
    } ~
      path("css" / Rest) { path =>
        getFromResource("api/public/css/" + path)
      } ~
      path("js" / Rest) { path =>
        getFromResource("api/public/js/" + path)
      } ~
      path("images" / Rest) { path =>
        getFromResource("api/public/images/" + path)
      } ~
      path("json" / Rest) { path =>
        getFromResource("api/public/json/" + path)
      }
  }
}