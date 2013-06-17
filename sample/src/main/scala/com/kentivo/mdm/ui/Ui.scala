package com.kentivo.mdm.ui

import com.kentivo.mdm.api.ApiDirectives
import akka.actor.ActorRefFactory

class Ui(implicit val actorRefFactory: ActorRefFactory) extends ApiDirectives {

  val route = {
      path("[^\\s]+\\.(?i)(?:jpg|png|ico|gif|bmp|html|css|js)$".r) { path =>
        getFromResource("public/" + path)
      } ~
      path("") { 
        getFromResource("public/index.html")
      } ~ 
      path(PathElement) { _ =>
        getFromResource("public/index.html")
      }  
  }
}