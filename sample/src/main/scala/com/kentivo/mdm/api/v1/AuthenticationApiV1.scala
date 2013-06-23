package com.kentivo.mdm.api.v1

import com.kentivo.mdm.api.ApiDirectives
import com.kentivo.mdm.logic.Authentication
import com.kentivo.mdm.logic.AuthenticationToken
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.actor.ActorContext
import spray.routing.RequestContext

case class AuthenticationUser(
  password: String,
  partyName: Option[String])

object AuthenticationApiV1 {
  val tokenKey = "Auth-Token"
}

/**
 * Handling authentication before using API.
 */
class AuthenticationApiV1 extends ApiDirectives {
  def route(implicit actorRefFactory: ActorRefFactory) : RequestContext => Unit =
    path("users" / PathElement / "authentication") { userName =>
      // Check if user is authenticated.
      get {
        headerValueByName(AuthenticationApiV1.tokenKey) { tokenValue =>
          Authentication.isAuthenticated(new AuthenticationToken(tokenValue)) match {
            case Some(user) => {
              val message = "User %s is logged in".format(userName)
              debug(message)
              complete {
                Map("message" -> message)
              }
            }
            case None => {
              val message = "User %s is not logged in".format(userName)
              debug(message)
              respondWithStatus(StatusCodes.NotFound) {
                complete {
                  Map("message" -> message)
                }
              }
            }
          }
        }
      } ~
        // Log in a specific user. Password will be in the body, in json format.  
        post {
          entity(as[AuthenticationUser]) { authenticationUser =>
            Authentication.authenticate(userName, authenticationUser.password, authenticationUser.partyName) match {
              case Some(AuthenticationToken(token)) => {
                val message = "User %s has been succesfully logged in".format(userName)
                debug(message)
                respondWithHeader(RawHeader(AuthenticationApiV1.tokenKey, token)) {
                  complete {
                    // Return authenticated user.
                    Authentication.isAuthenticated(AuthenticationToken(token)).getOrElse(null)
                  }
                }
              }
              case None => {
                debug("Tried to log in user %s but received 'Invalid userName or password.'".format(userName))
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
          headerValueByName(AuthenticationApiV1.tokenKey) { tokenValue =>
            Authentication.isAuthenticated(new AuthenticationToken(tokenValue)) match {
              case Some(user) => {
                Authentication.deAuthenticate(AuthenticationToken(tokenValue))
                val message = "User %s has been succesfully logged out".format(userName)
                debug(message)
                complete {
                  Map("message" -> message)
                }
              }
              case None => {
                val message = "User %s is not logged in".format(userName)
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