package com.busymachines.prefab.authentication.logic

import com.busymachines.prefab.authentication.model.{Authentication, Credentials}
import com.busymachines.commons.dao.{Versioned, Dao}
import com.busymachines.commons.CommonConfig
import com.busymachines.commons.domain.Id
import com.busymachines.commons.cache.Cache
import com.busymachines.prefab.authentication.db.AuthenticationDao
import com.busymachines.prefab.authentication.model.Authentication
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import org.joda.time.DateTime
import spray.json.JsonFormat

class AuthenticationConfig(baseName : String) extends CommonConfig(baseName) {
  val expiration = duration("expiration")
  val idleTime = duration("idleTime")
  val maxCapacity = int("maxCapacity")
}

abstract class PrefabAuthenticator[Principal, SecurityContext] (config: AuthenticationConfig, authenticationDao : AuthenticationDao) 
(implicit ec : ExecutionContext, principalFormat : JsonFormat[Principal]) {

  private type CachedData = Option[(Authentication, (Principal, SecurityContext))]
  
  private val cache = Cache.expiringLru[Id[Authentication], CachedData](config.maxCapacity, 50, config.expiration * 1.2, config.idleTime)

  protected def createSecurityContext(principal: Principal,id : Id[Authentication]) : Future[SecurityContext]

  def deauthenticate(id : Id[Authentication]) = 
    cache.remove(id)

  def authenticate(id : Id[Authentication]) : Future[Option[SecurityContext]] = 
    getOrFetchCachedData(id).map(_.map(_._2._2))

  def authenticate(id : Id[Authentication], timeout : Duration) : Option[SecurityContext] = 
    Await.result(getOrFetchCachedData(id), timeout).map(_._2._2)

  def setAuthenticated(principal : Principal) : Future[Id[Authentication]] = {
    val id : Id[Authentication] = Id.generate
    val authentication = Authentication(id, principalFormat.write(principal), new DateTime(config.expiration.toMillis))
    authenticationDao.createAuthentication(authentication).map(??? => id)
  }
  
  private def getOrFetchCachedData(id : Id[Authentication]) : Future[CachedData] = {
    cache(id) {
      authenticationDao.retrieveAuthentication(id).flatMap {
        case Some(authentication) =>
          val principal = principalFormat.read(authentication.principal)
          createSecurityContext(principal,id).map {
            securityContext =>
              Some((authentication, (principal, securityContext)))
          }
        case None =>
          Future.successful(None)
      }
    }
  }
}

