package com.busymachines.prefab.party.api.v1

import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.actor.ActorContext
import spray.routing.RequestContext
import com.busymachines.commons.spray.CommonHttpService
import scala.concurrent.duration._
import scala.concurrent.Await
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.logic.UserAuthenticator
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.api.v1.model.AuthenticationRequest
import com.busymachines.prefab.party.service.SecurityContext
import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.prefab.party.api.v1.model.AuthenticationResponse
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives
import com.busymachines.commons.implicits._
import com.busymachines.prefab.party.implicits._

/**
 * Handling authentication before using API.
 */
class AuthenticationApiV1(authenticator: UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with PartyApiV1Directives {
  
  def route: RequestContext => Unit =
    // Log in a specific user. Password will be in the body, in json format.  
    path("users" / "authentication") { 
      post {
        entity(as[AuthenticationRequest]) { request =>
          Await.result(authenticator.authenticateWithLoginNamePassword(request.loginName, request.password), 1.minute) match {
            case Some(SecurityContext(tenantId, partyId, userId, partyName, loginName, authenticationId, permissions)) => {
              val message = "User %s has been succesfully logged in".format(request.loginName)
              debug(message)
              respondWithHeader(RawHeader(AuthenticationDirectives.tokenKey, authenticationId.toString)) {
                complete {
                  AuthenticationResponse(authenticationId.toString, userId.toString, partyId.toString)
                }
              }
            }
            case None => {
              debug("Tried to log in user %s but received 'Invalid userName or password.'".format(request.loginName))
              respondWithStatus(StatusCodes.Forbidden) {
                complete {
                  Map("message" -> "Invalid userName or password.")
                }
              }
            }
          }
        }
      } 
    } ~    
    // Check if an authentication token is still valid
    path("users" / "authentication" / MatchId[Authentication]) { tokenValue =>
      get {
        Await.result(authenticator.authenticate(tokenValue), 1.minute) match {
          case Some(session) => {
            complete {
              Map("message" -> s"Authentication token is valid")
            }
          }
          case None => {
            respondWithStatus(StatusCodes.NotFound) {
              complete {
                Map("message" -> s"Authentication token is not valid")
              }
            }
          }
        }
      }~
      // Log out a specific user.
      delete {
          Await.result(authenticator.authenticate(tokenValue), 1.minute) match {
            case Some(securityContext) => {
              authenticator.deauthenticate(tokenValue)
              val message = s"User ${securityContext.user} has been succesfully logged out"
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