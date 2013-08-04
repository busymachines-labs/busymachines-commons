package com.kentivo.mdm.api.v1
import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.logic.PartiesManager
import akka.actor.ActorRefFactory
import spray.http.StatusCodes
import com.busymachines.commons.http.CommonHttpService
import com.kentivo.mdm.domain.User
import com.busymachines.commons.http.AbstractAuthenticator
import com.kentivo.mdm.api.UserAuthenticator

class PartiesApiV1(authenticator : UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiDirectives {
  val route =
    path("parties") {
      authenticate(authenticator) { implicit user =>
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
        path("parties" / MatchId[Party]) { entityId =>
          authenticate(authenticator) { implicit user =>
            complete {
              PartiesManager.find(entityId)
            }
          }
        }
      } ~
      delete {
        path("parties" / MatchId[Party]) { entityId =>
          authenticate(authenticator) { implicit user =>
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
