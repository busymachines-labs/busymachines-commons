package com.kentivo.mdm.logic

import com.busymachines.prefab.authentication.logic.AuthenticationConfig
import com.kentivo.mdm.db.UserDao
import com.busymachines.prefab.authentication.db.CredentialsDao
import com.busymachines.prefab.authentication.db.AuthenticationDao
import scala.concurrent.ExecutionContext
import com.busymachines.prefab.authentication.logic.PrefabAuthenticator
import com.busymachines.commons.Logging
import com.busymachines.commons.implicits._
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.DomainJsonFormats._
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.kentivo.mdm.db.PartyDao
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.prefab.authentication.model.Authentication

class UserAuthenticator(config: AuthenticationConfig, partyDao : PartyDao, credentialsDao: CredentialsDao, authenticationDao: AuthenticationDao)(implicit ec: ExecutionContext)
  extends PrefabAuthenticator[Id[User], SecurityContext](config, authenticationDao) with Logging {

  def createSecurityContext(userId: Id[User], id: Id[Authentication]): Future[Option[SecurityContext]] = {
    partyDao.findUserById(userId) map {
      case Some((party, user)) =>
        SecurityContext(party.id, user.id, user.loginName, id, Set.empty)
      case None =>  
        debug(s"Cannot authenticate user with id ${userId} as cannot find any user associated with it.")
        None
    }
  }
  
  def authenticateWithLoginNamePassword(loginName: String, password: String): Future[Option[SecurityContext]] =
    partyDao.findUserByLoginName(loginName) flatMap {
      case Some((party, user)) =>
        credentialsDao.retrieveWithPassword(user.credentials, password) flatMap {
          case Some(credentials) =>
            setAuthenticated(user.id) map {
              authenticationId =>
                SecurityContext(party.id, user.id, loginName, authenticationId, Set.empty)
            }
          case None =>
             debug(s"User $loginName credentials do not match provided password ****")
             Future.successful(None)
        }
      case None =>  
        debug(s"Cannot authenticate username $loginName as cannot find any user associated with it.")
        Future.successful(None)
  }
}