package com.kentivo.mdm.api.v1

import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.logic.UsersManager

import akka.actor.ActorRefFactory
import spray.http.StatusCodes

class UsersApiV1(implicit val actorRefFactory: ActorRefFactory) extends ApiDirectives {

  val route =
    path("users" / IdMatcher) { entityId =>
      authenticateUser { user =>
        put {
          entity(as[User]) { userUpdate =>
            UsersManager.update(entityId, userUpdate)
            respondWithStatus(StatusCodes.OK) {
              complete {
                ""
              }
            }
          }
        } ~
          get {
            complete {
              UsersManager.find(entityId)
            }
          }
      }
    }
}