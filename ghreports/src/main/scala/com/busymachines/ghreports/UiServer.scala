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
import spray.routing.directives.ExecutionDirectives
import spray.routing.directives.MethodDirectives
import akka.actor.ActorRefFactory
import spray.routing.directives.RouteDirectives
import spray.routing.directives.RespondWithDirectives
import spray.routing.directives.MiscDirectives
import spray.routing.directives.FileAndResourceDirectives
import spray.http.ContentType
import spray.http.HttpCharsets


class UiServer(implicit actorSystem: ActorSystem) extends Ui with Logging {
  class Actor extends HttpServiceActor {
    def receive = runRoute(route)
//        def receive = runRoute(logRequest(showRequest _) {route})
    def showRequest(request: HttpRequest) = LogEntry("URL: " + request.uri + "\n CONTENT: " + request.entity, InfoLevel)

  }

  def actorRefFactory = actorSystem
      
  implicit val rs = RoutingSettings(actorSystem)
      
  def start = 
    IO(Http) ! Http.Bind(actorSystem.actorOf(Props(new Actor), "uiserver"), interface = "localhost", port = 8080)
}


trait ProcessedResourceDirectives  {
  import collection.mutable
  import akka.actor.ActorRefFactory
  import ExecutionDirectives._
  import MethodDirectives._
  import RespondWithDirectives._
  import RouteDirectives._
  import MiscDirectives._
  import FileAndResourceDirectives._
  import spray.util._
  import spray.httpx.marshalling.{ Marshaller, BasicMarshallers }
  import org.parboiled.common.FileUtils
  
  private val cache = mutable.Map[String, Array[Byte]]()
  
  def getFromResource2(resourceName : String)(implicit refFactory: ActorRefFactory) : Route = {
      resourceName.lastIndexOf('.') match {
        case -1 => 
          getFromResource(resourceName, "", false)
        case i if i > 4 && resourceName.substring(i - 4, i) == "-???" =>
          getFromResource(resourceName.substring(0, i - 4), resourceName.substring(i + 1), true)
        case i =>
          getFromResource(resourceName.substring(0, i), resourceName.substring(i + 1), false)
      }
  }
  
  def getFromResource(basename: String, extension: String, process: Boolean)(implicit refFactory: ActorRefFactory) : Route = {
    val mediaType =  MediaTypes.forExtension(extension).getOrElse(MediaTypes.`application/octet-stream`)
    val contentType = if (mediaType.binary) ContentType(mediaType)
    else ContentType(mediaType,  HttpCharsets.`UTF-8`)
    get {
      detachTo(singleRequestServiceActor) {
        val theClassLoader = actorSystem.dynamicAccess.classLoader
        theClassLoader.getResource(basename + "." + extension) match {
          case null => reject
          case url =>
            val lastModified = url.openConnection.getLastModified
            implicit val bufferMarshaller = BasicMarshallers.byteArrayMarshaller(contentType)
            respondWithLastModifiedHeader(lastModified) {
              println("Reading bytes")
              complete(FileUtils.readAllBytes(url.openStream))
            }
        }
      }
    }
  }
}

trait Ui extends HttpService with ProcessedResourceDirectives {


  val route = {
    path("") { 
      println("Resource emp")
      getFromResource2("public/index-???.html")
    } ~ 
    path(Rest) { _ match {
      case path if path.endsWith(".js") => 
//        respondWithMediaType(MediaTypes.`application/javascript`) {
          getFromResource2("public/" + path)
//        }
      case path if path.endsWith(".css") => 
//        respondWithMediaType(MediaTypes.`text/css`) {
          getFromResource2("public/" + path)
//        }
      case _ => getFromResource2("public/index.html")
    }}  
  }
}
