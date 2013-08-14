package com.busymachines.commons.http

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong
import scala.language.postfixOps
import spray.caching.LruCache
import spray.http.HttpCredentials
import spray.http.HttpRequest
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator
import java.math.BigInteger
import java.security.SecureRandom
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import spray.routing.Route
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

case class AuthenticationToken(token: String)

class CommonAuthenticator2[A](implicit val ec: ExecutionContext) {

  protected def configBaseName = classOf[com.busymachines.commons.http.CommonAuthenticator[_]].getPackage.getName + ".authentication"
  private val config = ConfigFactory.load(configBaseName)
  private val secureRandom = new SecureRandom

  val expirationMillis = config.getMilliseconds("expiration").longValue
  val idleTimeMillis = config.getMilliseconds("idleTime").longValue

  private val cache = CacheBuilder.newBuilder.
    expireAfterAccess(idleTimeMillis, TimeUnit.MILLISECONDS).
    expireAfterWrite(expirationMillis, TimeUnit.MILLISECONDS).build()

  def authenticate(token : AuthenticationToken) = {} 

}

class CommonAuthenticator[A](implicit val executionContext: ExecutionContext) extends HttpAuthenticator[A] {

  def configBaseName = classOf[com.busymachines.commons.http.CommonAuthenticator[_]].getPackage.getName + ".authentication"
  val config = ConfigFactory.load(configBaseName)
  val maxCapacity = config.getInt("maxCapacity")
  val expiration = config.getMilliseconds("expiration").longValue.millis
  val idleTime = config.getMilliseconds("idleTime").longValue.millis
  val secureRandom = new SecureRandom

  private val cache = LruCache[A](maxCapacity, 50, expiration, idleTime)

  val tokenKey = "Auth-Token"

  override def apply(ctx: RequestContext) = {
    ctx.request.headers.find(_.is(tokenKey)).map(_.value) match {
      case Some(token) =>
        authenticateToken(token, ctx) map {
          case Some(a) => Right(a)
          case None =>
            Left(AuthenticationFailedRejection(CredentialsRejected, CommonAuthenticator.this))
        }
      case None =>
        Future.successful(Left(AuthenticationFailedRejection(CredentialsMissing, CommonAuthenticator.this)))
    }
  }

  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext): Future[Option[A]] = {
    throw new Exception
  }

  def isAuthenticated(timeout: Duration = 1.minute)(f: Option[A] => Route): Route = { ctx =>
    import spray.routing.HttpService._
    val auth = ctx.request.headers.find(_.is(tokenKey)).map(_.value).map(authenticateToken(_, ctx))
    f(auth.flatMap(auth => Await.result(auth, timeout)))
  }

  def authenticateToken(token: String, ctx: RequestContext): Future[Option[A]] = {
    cache.get(token) match {
      case Some(a) => a.map(Some(_))
      case None => Future.successful(None)
    }
  }

//  def addAuthentication(a: A) {
//    val token = generateToken
//    cache.apply(token, a)
//  }

  def getChallengeHeaders(httpRequest: HttpRequest) = Nil

  def generateToken =
    (new BigInteger(130, secureRandom)).toString(32)
}