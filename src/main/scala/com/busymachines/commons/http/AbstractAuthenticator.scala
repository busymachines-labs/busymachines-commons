package com.busymachines.commons.http

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import spray.caching.LruCache
import spray.http.HttpCredentials
import spray.http.HttpRequest
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator

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