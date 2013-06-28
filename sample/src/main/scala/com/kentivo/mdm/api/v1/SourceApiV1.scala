package com.kentivo.mdm.api.v1

import com.kentivo.mdm.api.ApiDirectives
import com.busymachines.commons
import com.kentivo.mdm.domain.Source
import com.kentivo.mdm.logic.SourceManager
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import spray.routing.HttpService
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import spray.httpx.SprayJsonSupport._ 
 
class SourceApiV1(sourceManager: SourceManager) extends ApiDirectives {
  
  def route(implicit actorRefFactory: ActorRefFactory) = path("sources") {
    authenticateUser { implicit user =>
      get {
        complete {
          sourceManager.findSources(None)
        }
      } ~ post {
        entity(as[Source]) { entity =>
          complete {
            ""
          }
        }
      }
    }
  } ~
    path("sources" / PathElement) { entityId =>
      authenticateUser { implicit user =>
        put {
          entity(as[Source]) { source =>
            sourceManager.update(Id(entityId), source)
            respondWithStatus(StatusCodes.OK) {
              complete {
                ""
              }
            }
          }
        } ~
          get {
            complete {
              sourceManager.find(Id(entityId))
            }
          }
      }
    }
}