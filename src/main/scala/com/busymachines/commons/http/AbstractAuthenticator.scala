package com.busymachines.commons.http

import spray.routing.Route
import scala.concurrent.duration._
import spray.caching.LruCache
import spray.routing.HttpService._
import spray.routing.AuthorizationFailedRejection
import spray.routing.authentication.HttpAuthenticator
import scala.concurrent.ExecutionContext
import spray.http.HttpRequest
import spray.http.HttpCredentials
import spray.routing.RequestContext
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import scala.concurrent.Future

class AbstractAuthenticator[A](implicit val executionContext: ExecutionContext) extends HttpAuthenticator[A] {

  val maxCapacity = 1000000
  val expiration = 1 day
  val idleTime = 1 hour

  private val cache = LruCache[A](maxCapacity, 50, expiration, idleTime)

  val tokenKey = "Auth-Token"

  override def apply(ctx: RequestContext) = {
       ctx.request.headers.find(_.is(tokenKey)).map(_.value) match {
        case Some(token) =>
          authenticate(token, ctx) map {
            case Some(a) => Right(a)
            case None =>
            Left(AuthenticationFailedRejection(CredentialsRejected, this))
          }
        case None =>
           Future.successful(Left(AuthenticationFailedRejection(CredentialsMissing, this)))
      }
  }
  
  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext) : Future[Option[A]] = {
    throw new Exception
  }
  
  def authenticate(token: String, ctx: RequestContext) : Future[Option[A]] = {
    cache.get(token) match {
      case Some(a) => a.map(Some(_))
      case None => Future.successful(None)
    }
  }
  
  def getChallengeHeaders(httpRequest: HttpRequest) = Nil
}