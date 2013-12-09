package com.busymachines.prefab.party.logic

import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import com.busymachines.prefab.authentication.db.CredentialsDao
import com.busymachines.prefab.authentication.db.AuthenticationDao
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.busymachines.prefab.authentication.logic.PrefabAuthenticator
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.db.PartyDao
import com.busymachines.prefab.party.service.SecurityContext
import com.busymachines.prefab.party.domain.PartyDomainJsonFormats._
import com.busymachines.prefab.party.domain.User

class UserAuthenticator(config: AuthenticationConfig, partyDao : PartyDao, credentialsDao: CredentialsDao, authenticationDao: AuthenticationDao)(implicit ec: ExecutionContext)
  extends PrefabAuthenticator[Id[Credentials], SecurityContext](config, authenticationDao) with Logging {

  def createSecurityContext(credentialsId: Id[Credentials], id: Id[Authentication]): Future[Option[SecurityContext]] = {
    partyDao.findUserByCredentialsId(credentialsId) map {
      case Some((party, user)) =>
        val permissions = party.userRoles.filter(role => user.roles.contains(role.id)).flatMap(_.permissions).toSet
        SecurityContext(tenantId = party.tenant, party.id, user.id, party.describe, user.describe, id, permissions) 
      case None =>  
        debug(s"Cannot authenticate user with credentials: ${credentialsId}.")
        None
    }
  }
  
  /**
   * Use this method when a security context is need, but there is no physical user that
   * goes through the login process. For example, a batch process.
   */
  def securityContextFor(partyId : Id[Party], userId : Id[User]) : Future[SecurityContext] = {
    partyDao.retrieve(partyId).map { 
      case Some(Versioned(party, version)) =>
        val user = party.users.find(_.id == userId).getOrElse(throw new Exception(s"User $userId not found"))
        val permissions = party.userRoles.filter(role => user.roles.contains(role.id)).flatMap(_.permissions).toSet
        SecurityContext(tenantId = party.tenant, party.id, userId, party.describe, user.describe, Id(""), permissions)
      case _ => throw new Exception(s"Party $partyId not found")
    }    
  }
  
  override def devmodeSecurityContext(devmodeAuth : Option[String]) : Option[SecurityContext] =
    devmodeAuth.flatMap { loginName =>
      credentialsDao.findByLogin(loginName).await(1.minute).headOption.flatMap { 
        case Versioned(credentials, _) => 
          val authenticationId = setAuthenticated(credentials.id).await(1.minute) 
          val context = createSecurityContext(credentials.id, authenticationId).await(1.minute)
          debug(s"Authenticated test user: $loginName")
          context
      }
    }
    
  /**
   * Authenticates a user of a specific party.
   */
  def authenticateWithLoginNamePassword(partyId : Id[Party], login: String, password: String): Future[Option[SecurityContext]] = 
    partyDao.retrieve(partyId).flatMap { party =>
      authenticateWithLoginNamePassword(login, password, (c: Credentials) => party.isDefined && party.get.users.exists(_.credentials == c.id))
    }
  
  /**
   * Authenticates a user regardless of the party it belongs to.
   */
  def authenticateWithLoginNamePassword(loginName: String, password: String, validate : Credentials => Boolean = _ => true): Future[Option[SecurityContext]] = {
    debug(s"Trying to authenticate with username $loginName and password ****")
    credentialsDao.findByLogin(loginName).flatMap {
      credentials =>
        credentials.
          map(_.entity).
          filter(_.passwordCredentials.exists(p => p.login == loginName && p.hasPassword(password))).
          filter(validate).
          headOption match {
          case Some(credentials) => 
            setAuthenticated(credentials.id) flatMap {
              authenticationId =>
                createSecurityContext(credentials.id, authenticationId)
            }
          case None => 
            debug(s"Cannot authenticate $loginName.")
            Future.successful(None)
        }
    }
  }
}