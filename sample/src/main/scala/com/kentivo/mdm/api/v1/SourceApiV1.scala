package com.kentivo.mdm.api.v1

import com.busymachines.commons
import com.kentivo.mdm.domain.Source
import com.kentivo.mdm.logic.SourceManager
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import spray.routing.HttpService
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import spray.httpx.SprayJsonSupport._
import com.busymachines.commons.http.CommonHttpService
import com.busymachines.commons.domain.CommonJsonFormats._
import com.kentivo.mdm.domain.DomainJsonFormats._
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.logic.UserAuthenticator
 
class SourceApiV1(sourceManager: SourceManager, authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiV1Directives {
  
  val route = path("sources") {
    authenticate(authenticator) { implicit securityContext =>
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
    path("sources" / Segment) { entityId =>
      authenticate(authenticator) { implicit user =>
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