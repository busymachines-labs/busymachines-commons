package com.kentivo.mdm.api.v1

import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.actor.ActorContext
import spray.routing.RequestContext
import com.busymachines.commons.http.CommonHttpService
import scala.concurrent.duration._
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.logic.UserAuthenticator
import scala.concurrent.Await
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.api.v1.model.AuthenticationRequest
import com.kentivo.mdm.logic.SecurityContext
import com.kentivo.mdm.logic.UserAuthenticator

/**
 * Handling authentication before using API.
 */
class AuthenticationApiV1(authenticator: UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with ApiV1Directives {
  
  def route: RequestContext => Unit =
//    path("users" / "authentication") { 
//      post {
//       entity(as[Credentials]) { 
//         case Credentials(email, password, _) =>
//           authenticator.authenticateUser(email, password).map {
//             case Some()
//           }
//       }
//      }
//    } ~
    path("users" / MatchId[User] / "authentication") { userId =>
      // Check if user is authenticated.
      get {
        headerValueByName(tokenKey) { tokenValue =>
          Await.result(authenticator.authenticate(Id(tokenValue)), 1.minute) match {
            case Some(session) => {
              complete {
                Map("message" -> s"Partner with token $tokenValue is logged in")
              }
            }
            case None => {
              respondWithStatus(StatusCodes.NotFound) {
                complete {
                  Map("message" -> s"Partner with token $tokenValue is not logged in")
                }
              }
            }
          }
        }
      } ~
        // Log in a specific user. Password will be in the body, in json format.  
        post {
          entity(as[AuthenticationRequest]) { request =>
            Await.result(authenticator.authenticateWithLoginNamePassword(request.loginName, request.password), 1.minute) match {
              case Some(SecurityContext(partyId, userId, loginName, authenticationId, permissions)) => {
                val message = "User %s has been succesfully logged in".format(request.loginName)
                debug(message)
                respondWithHeader(RawHeader(tokenKey, authenticationId.toString)) {
                  complete {
                    // Return authenticated user id.
                        Map("userId" -> userId)
                  }
                }
              }
              case None => {
                debug("Tried to log in user %s but received 'Invalid userName or password.'".format(request.loginName))
                respondWithStatus(StatusCodes.NotFound) {
                  complete {
                    Map("message" -> "Invalid userName or password.")
                  }
                }
              }
            }
          }
        } ~
        // Log out a specific user.
        delete {
          headerValueByName(tokenKey) { tokenValue =>
            Await.result(authenticator.authenticate(Id(tokenValue)), 1.minute) match {
              case Some(securityContext) => {
                authenticator.deauthenticate(Id(tokenValue))
                val message = "User %s has been succesfully logged out".format(securityContext.loginName)
                debug(message)
                complete {
                  Map("message" -> message)
                }
              }
              case None => {
                val message = "User already logged out."
                debug(message)
                respondWithStatus(StatusCodes.NotFound) {
                  complete {
                    Map("message" -> message)
                  }
                }
              }
            }
          }
        }
    }
}