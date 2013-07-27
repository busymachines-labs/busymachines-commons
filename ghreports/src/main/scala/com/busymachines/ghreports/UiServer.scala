package com.busymachines.ghreports

import com.busymachines.commons.Logging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging.InfoLevel
import akka.io.IO
import spray.can.Http
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.routing.ExceptionHandler
import spray.routing.HttpServiceActor
import spray.routing.Route
import spray.routing.RoutingSettings._
import spray.routing.HttpService
import spray.routing.RoutingSettings
import com.typesafe.config.ConfigFactory
import spray.http.HttpRequest
import spray.routing.directives.LogEntry
import spray.http.MediaType
import spray.http.MediaTypes


class UiServer(implicit actorSystem: ActorSystem) extends Ui with Logging {
  class Actor extends HttpServiceActor {
    def receive = runRoute(logRequest(showRequest _) {route})
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, InfoLevel)

  }

  def actorRefFactory = actorSystem
      
  implicit val rs = RoutingSettings(actorSystem)
      
  def start = 
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), "uiserver"), interface = "localhost", port = 8080)
}

trait Ui extends HttpService {

  val route = {
    path("") { 
      println("Resource emp")
      getFromResource("public/index.html")
    } ~ 
    path(Rest) { _ match {
      case path if path.endsWith(".js") => 
        respondWithMediaType(MediaTypes.`application/javascript`) {
          getFromResource("public/" + path)
        }
      case path if path.endsWith(".css") => 
        respondWithMediaType(MediaTypes.`text/css`) {
          getFromResource("public/" + path)
        }
      case _ => getFromResource("public/index.html")
    }}  
  }
}
