package com.busymachines.prefab.party.api.v1

import com.busymachines.commons.spray.CommonHttpService
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.logic.UserAuthenticator
import com.busymachines.prefab.party.service.PartyService

class PartiesApiV1(partyService : PartyService, authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with PartyApiV1Directives {
  val route =
    path("parties") {
      authenticate(authenticator) { implicit securityContext =>
        get {
          complete {
            partyService.listParties
          }
        } ~
          post {
            entity(as[Party]) { party =>
              complete {
               partyService.createParty(party) map ( p => Map("id" -> p.id))
              }
            }
          }
      }
    } ~
      get {
        path("parties" / MatchId[Party]) { entityId =>
          authenticate(authenticator) { implicit usauther =>
            complete {
              partyService.getParty(entityId)
            }
          }
        }
      } ~
      delete {
        path("parties" / MatchId[Party]) { entityId =>
          authenticate(authenticator) { implicit user =>
            partyService.deleteParty(entityId)
            respondWithStatus(StatusCodes.OK) {
              complete {
                ""
              }
            }
          }
        }
      }
}
