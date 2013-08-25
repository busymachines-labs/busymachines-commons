package com.busymachines.prefab.authentication.http

import spray.routing.Route
import com.busymachines.prefab.authentication.logic.PrefabAuthenticator
import spray.routing.HttpService._
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.commons.NotAuthorizedException
import scala.concurrent.duration.Duration
import com.busymachines.commons.NotAuthenticatedException

trait AuthenticationDirectives {

  val tokenKey = "Auth-Token"

  def authenticationRoute[Principal, SecurityContext](authenticator : PrefabAuthenticator[Principal, SecurityContext], timeout : Duration)(nested : SecurityContext => Route) : Route = {
    ctx =>
	    ctx.request.headers.find(_.equals(tokenKey)).map(_.value).map(Id[Authentication](_)) match {
	      case Some(authenticationId) =>
	        authenticator.authenticate(authenticationId, timeout) match {
	          case Some(securityContext) =>
	            nested(securityContext)
	          case None =>
	            throw new NotAuthenticatedException("Authentication failed")
	        }
	      case None =>
	        throw new NotAuthenticatedException("No authentication token found")
	    }
  }
}