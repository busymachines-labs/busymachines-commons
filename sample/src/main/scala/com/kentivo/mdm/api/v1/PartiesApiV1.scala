package com.kentivo.mdm.api.v1

import com.busymachines.commons.http.CommonHttpService
import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.logic.PartyService
import akka.actor.ActorRefFactory
import com.kentivo.mdm.domain.Party
import spray.http.StatusCodes
import com.kentivo.mdm.logic.UserAuthenticator

class PartiesApiV1(partyService : PartyService, authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiDirectives {
  val route =
    path("parties") {
      authenticate(authenticator) { implicit securityContext =>
        get {
          complete {
            partyService.list
          }
        } ~
          post {
            entity(as[Party]) { entity =>
              val partyId = partyService.create(entity)
              complete {
                Map("id" -> partyId)
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
            partyService.delete(entityId)
            respondWithStatus(StatusCodes.OK) {
              complete {
                ""
              }
            }
          }
        }
      }
}
