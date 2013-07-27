package com.kentivo.mdm.logic

import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.typesafe.config.ConfigFactory
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import com.kentivo.mdm.domain.User

case class AuthenticationToken(token: String)

object Authentication extends Logging {
  val secureRandom = new SecureRandom()
  lazy val config = ConfigFactory.load.getConfig("com.kentivo.mdm.authentication")
  val expiryTimeSec = config.getLong("expiryTimeSec")
  
  val demoUser : Option[User] = config.getIntOption("demoUserId") flatMap { id =>
    info(s"Using demo user id $id")
    None
  } 

  private val authenticatedUsers: Cache[AuthenticationToken, User] =
    CacheBuilder.newBuilder.expireAfterAccess(expiryTimeSec, TimeUnit.SECONDS).build[AuthenticationToken, User]

  /**
   * Logic for doing authentication of a specific user. If succeeds, returns primary key of that user.
   */
  def authenticate(username: String, password: String, partyName: Option[String]): Option[AuthenticationToken] = {
    None
  }
  
  /**
   * Clear a specific token from cache. Used to log-out a user.
   */
  def deAuthenticate(token: AuthenticationToken) = {
      authenticatedUsers.invalidate(token)
  }

  /**
   * Check if a specific token exists in cache. Used to check if a user is logged-in.
   */
  def isAuthenticated(token: AuthenticationToken): Option[User] = {
    debug("Authentication token: " + token)
    authenticatedUsers.getIfPresent(token) match {
      case null => demoUser
      case user => Some(user)
    }
    Some(User())
  }

  def getSecureId = {
    new BigInteger(130, secureRandom).toString(32);
  }
}