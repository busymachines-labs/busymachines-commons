package com.kentivo.mdm.api.v1
import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.logic.PartiesManager

import akka.actor.ActorRefFactory
import spray.http.StatusCodes

class PartiesApiV1 extends ApiDirectives {
  def route(implicit actorRefFactory: ActorRefFactory) =
    path("parties") {
      authenticateUser { implicit user =>
        get {
          complete {
            PartiesManager.list
          }
        } ~
          post {
            entity(as[Party]) { entity =>
              val partyId = PartiesManager.create(entity)
              complete {
                Map("id" -> partyId)
              }
            }
          }
      }
    } ~
      get {
        path("parties" / IdMatcher) { entityId =>
          authenticateUser { implicit user =>
            complete {
              PartiesManager.find(entityId)
            }
          }
        }
      } ~
      delete {
        path("parties" / IdMatcher) { entityId =>
          authenticateUser { implicit user =>
            PartiesManager.delete(entityId)
            respondWithStatus(StatusCodes.OK) {
              complete {
                ""
              }
            }
          }
        }
      }
}
