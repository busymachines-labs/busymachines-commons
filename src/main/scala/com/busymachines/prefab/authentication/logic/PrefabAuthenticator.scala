package com.busymachines.prefab.authentication.logic

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import org.joda.time.DateTime

import com.busymachines.commons.CommonConfig
import com.busymachines.commons.cache.Cache
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.db.AuthenticationDao
import com.busymachines.prefab.authentication.model.Authentication

import spray.json.JsonFormat

class AuthenticationConfig(baseName: String) extends CommonConfig(baseName) {
  val expiration = duration("expiration")
  val idleTime = duration("idleTime")
  val maxCapacity = int("maxCapacity")
}

/**
 * Base class for application-specific authenticators. It caches authentications and makes sure
 * authentication works in a clustered environment.
 * <p>
 * A Principal identifies the user (or other entity) that can be authenticated.
 * In many situations the principal can simply be Id[Credentials]. An implementation might
 * choose some other data, for example in situations where different kinds of entities
 * can be authenticated (e.g. end-users versus employees).
 * <p>
 * A SecurityContext is an application-specific data structure that is used by
 * logic components to hold information about the currently authenticated entity.
 * A security context typically holds the principal and the authentication id, but 
 * this is not a requirement.
 */
abstract class PrefabAuthenticator[Principal, SecurityContext](config: AuthenticationConfig, authenticationDao: AuthenticationDao)(implicit ec: ExecutionContext, principalFormat: JsonFormat[Principal]) {

  private type CachedData = Option[(Principal, SecurityContext)]

  private val cache = Cache.expiringLru[Id[Authentication], CachedData](config.maxCapacity, 50, config.expiration * 1.2, config.idleTime)

  /**
   * Re-authenticates based on given authentication id (token).
   */ 
  def authenticate(id: Id[Authentication], timeout: Duration): Option[SecurityContext] =
    Await.result(getOrFetchCachedData(id), timeout).map(_._2)

  /**
   * Re-authenticates based on given authentication id (token).
   * Asynchronous version.
   */
  def authenticate(id: Id[Authentication]): Future[Option[SecurityContext]] =
    getOrFetchCachedData(id).map(_.map(_._2))

  /** 
   * De-authenticates.
   */
  def deauthenticate(id: Id[Authentication]) : Future[Unit] =
    (authenticationDao.delete(id) flatMap { _ =>
      cache.remove(id) match {
        case None => Future.successful(None)
        case Some(f) => f
      }
    }) map {_ => }

  /**
   * Marks given principal authenticated. This is the only method that can be
   * used to retrieve an authentication id (token). It is typically called from 
   * the sub-class, from 'concrete' authentication methods, like 
   * 'loginWithUsernamePassword'.
   */
  def setAuthenticated(principal: Principal): Future[Id[Authentication]] = {
    val id: Id[Authentication] = Id.generate
    val authentication = Authentication(id, principalFormat.write(principal), new DateTime(config.expiration.toMillis))
    authenticationDao.createAuthentication(authentication).map(??? => id)
  }
  
  /**
   * Can be overridden to create a dev-mode security context. This allows bypassing login-sequences
   * while developing applications.
   */
  protected[authentication] def devmodeSecurityContext(devmodeAuth : Option[String]) : Option[SecurityContext] = 
    None
  
  /**
   * Should be overridden to creates a security context from given principal.
   */
  protected def createSecurityContext(principal: Principal, id: Id[Authentication]): Future[Option[SecurityContext]]

  private def getOrFetchCachedData(id : Id[Authentication]): Future[CachedData] = {
    cache(id) {
      authenticationDao.retrieveAuthentication(id).flatMap {
        case Some(authentication) =>
          val principal = principalFormat.read(authentication.principal)
          createSecurityContext(principal, id).map {
            case Some(securityContext) =>
              Some(principal, securityContext)
            case None =>
              None
          }
        case None =>
          Future.successful(None)
      }
    }
  }
}

