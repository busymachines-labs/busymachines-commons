package com.kentivo.mdm.api.v1

import com.kentivo.mdm.domain.User
import com.kentivo.mdm.logic.UsersManager
import com.kentivo.mdm.logic.PartyService
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import com.busymachines.commons.http.CommonHttpService
import com.kentivo.mdm.logic.UserAuthenticator

class UsersApiV1(partyService : PartyService, authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiV1Directives {

  val route =
    path("users") {
      authenticate(authenticator) { user => 
          get {
            complete {
              partyService.getParty(user.partyId).map(_.map(_.users))
            }
          }
      }
    } ~
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