package com.busymachines.prefab.party.logic

import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import com.busymachines.prefab.authentication.db.CredentialsDao
import com.busymachines.prefab.authentication.db.AuthenticationDao
import scala.concurrent.ExecutionContext
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

class UserAuthenticator(config: AuthenticationConfig, partyDao : PartyDao, credentialsDao: CredentialsDao, authenticationDao: AuthenticationDao)(implicit ec: ExecutionContext)
  extends PrefabAuthenticator[Id[Credentials], SecurityContext](config, authenticationDao) with Logging {

  def createSecurityContext(credentialsId: Id[Credentials], id: Id[Authentication]): Future[Option[SecurityContext]] = {
    partyDao.findUserByCredentialsId(credentialsId) map {
      case Some((party, user)) =>
        SecurityContext(tenantId = party.tenant, party.id, user.id, user.describe, id, Set.empty) 
      case None =>  
        debug(s"Cannot authenticate user with credentials: ${credentialsId}.")
        None
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