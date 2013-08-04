package com.kentivo.mdm.api.v1

import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.logic.UsersManager
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import com.busymachines.commons.http.CommonHttpService
import com.busymachines.commons.http.AbstractAuthenticator
import com.kentivo.mdm.api.UserAuthenticator

class UsersApiV1(authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiDirectives {

  val route =
    path("users" / MatchId[User]) { entityId =>
      authenticate(authenticator) { user =>
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