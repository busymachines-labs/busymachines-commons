package com.busymachines.prefab.authentication.logic

import com.busymachines.commons.{ NotAuthorizedException, CommonConfig}
import com.busymachines.commons.util.AsyncCache
import com.busymachines.commons.Implicits._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import org.joda.time.DateTime
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.Implicits._
import com.busymachines.prefab.authentication.db.{CredentialsDao, AuthenticationDao}
import com.busymachines.prefab.authentication.model.{PasswordCredentials, Credentials, Authentication}

import spray.json.JsonFormat

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
abstract class CredentialsAuthenticator[SecurityContext](config: AuthenticationConfig, authenticationDao: AuthenticationDao, credentialsDao: CredentialsDao)(implicit ec: ExecutionContext)
  extends PrefabAuthenticator[Id[Credentials], SecurityContext](config, authenticationDao) {

  def authenticateWithLoginName(loginName: String, validate : Credentials => Boolean = _ => true): Future[SecurityContext] = {
    logger.debug(s"Trying to authenticate with username $loginName and password ****")
    credentialsDao.findByLogin(loginName).flatMap {
      _.find(validate) match {
        case Some(credentials) =>
          setAuthenticated(credentials.id) flatMap {
            authenticationId =>
              createSecurityContext(credentials.id, authenticationId).map(_.getOrElse {
                throw new NotAuthorizedException("Invalid username or password")
              })
          }
        case None =>
          throw new NotAuthorizedException("Invalid username or password")
      }
    }
  }

  /**
   * Authenticates a user regardless of the party it belongs to.
   */
  def authenticateWithLoginNamePassword(loginName: String, password: String, validate : Credentials => Boolean = _ => true): Future[SecurityContext] =
    authenticateWithLoginName(loginName, credentials =>
      credentials.passwordCredentials.exists(p => p.login == loginName && p.hasPassword(password)) && validate(credentials))


  override protected[authentication] def devmodeSecurityContext(devmodeAuth : Option[String]) : Option[SecurityContext] =
    devmodeAuth.map { auth =>
      Await.result(authenticateWithLoginName(auth), 1.minute)
    }
}

