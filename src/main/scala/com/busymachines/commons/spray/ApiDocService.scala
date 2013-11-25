package com.busymachines.commons.spray

import akka.actor.ActorRefFactory
import spray.http.StatusCodes

class ApiDocService(resourceRoot : List[String] = "apidoc" :: Nil)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService {
  val prefix = resourceRoot match {
    case Nil => ""
    case list => list.mkString("", "/", "/")
  }
  val route = {
    pathSingleSlash {
      redirect("api.html", StatusCodes.TemporaryRedirect)
    } ~
    pathEnd { 
      extract (_.request.uri) { uri =>
        redirect(uri.withPath(uri.path + "/api.html"), StatusCodes.TemporaryRedirect)
      }
    } ~
    path("api.html") {  
      getFromResource(prefix + "html/restApiDoc.html")
    } ~
    pathPrefix("api") {
      path("css" / Rest) { path =>
        getFromResource(prefix + "css/" + path)
      } ~
      path("js" / Rest) { path =>
        getFromResource(prefix + "js/" + path)
      } ~
      path("images" / Rest) { path =>
        getFromResource(prefix + "images/" + path)
      } ~ path("json" / Rest) { path =>
        getFromResource(prefix + "json/" + path)
      }
    }
  }
}