package com.busymachines.prefab.party.api.v1

import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.service.PartyService
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import com.busymachines.commons.spray.CommonHttpService
import com.busymachines.prefab.party.logic.UserAuthenticator

class UsersApiV1(partyService : PartyService, authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with PartyApiV1Directives {

  val route =
    path("users") {
      authenticate(authenticator) { implicit securityContext =>  
          get {
            complete {
              partyService.getParty(securityContext.partyId).map(_.map(_.users))
            }
          }
      }
    } ~
    path("users" / MatchId[User]) { userId =>
      authenticate(authenticator) { implicit securityContext =>  
        put {
          entity(as[User]) { userUpdate =>
            respondWithStatus(StatusCodes.OK) {
              complete {
                partyService.updateUser(userId, userUpdate) map (_=>"OK")
              }
            }
          }
        } ~
          get {
            complete {
              partyService.findUser(userId)
            }
          }
      }
    }
}