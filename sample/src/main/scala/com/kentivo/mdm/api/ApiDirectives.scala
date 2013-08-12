package com.kentivo.mdm.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.kentivo.mdm.api.v1.AuthenticationApiV1
import com.kentivo.mdm.logic.AuthenticationToken
import spray.httpx.SprayJsonSupport
import spray.routing.authentication.Authentication
import spray.routing.AuthenticationFailedRejection
import spray.routing.Directives
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.routing.authentication.ContextAuthenticator
import spray.routing.directives.AuthMagnet.fromContextAuthenticator
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.DomainJsonFormats
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.actor.ActorContext
import scala.concurrent.ExecutionContext
import com.busymachines.commons.Logging
import com.busymachines.commons.domain.CommonJsonFormats
import spray.routing.Route

/**
 * Base trait for individual API service traits.
 */
trait ApiDirectives extends Directives with Logging with SprayJsonSupport with ApiJsonFormats with DomainJsonFormats with CommonJsonFormats  { 

//  implicit def actorRefFactory: ActorRefFactory
//  implicit def executionContext: ExecutionContext = actorRefFactory.dispatcher
//    implicit def executionContext(implicit actorRefFactory: ActorRefFactory) = actorRefFactory.dispatcher

//  val route : RequestContext => Unit 
  
//  val myAuthenticator: ContextAuthenticator[User] = { ctx => Future(doAuthenticate(ctx)) }
//
//  def doAuthenticate(ctx: RequestContext): Authentication[User] = {
//    debug("--do auth: ")
//    // Take authToken from header or queryParam.
//    val tokenValue = ctx.request.headers.filter(header => header.name == AuthenticationApiV1.tokenKey.toLowerCase()).headOption.map(_.value).
//      getOrElse(ctx.request.uri.query.filter(param => param._1 == "authToken").headOption.map(_._2).getOrElse(""))
//    debug("--token value " + tokenValue)
//    Authentication.isAuthenticated(new AuthenticationToken(tokenValue)) match {
//      case Some(user) => Right(user)
//      case None => Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, myAuthenticator))
//    }
//  }
//
//  def authenticateUser = Directives.authenticate(myAuthenticator)
//  
//  def auth(nested : => Route) : Route = {
//    request => 
//      val i : Int = request.request.headers.find(_.is(AuthenticationApiV1.tokenKey)).map(_.value)
//  }
}