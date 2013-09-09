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

class UserAuthenticator(config: AuthenticationConfig, partyDao : PartyDao, credentialsDao: CredentialsDao, authenticationDao: AuthenticationDao)(implicit ec: ExecutionContext)
  extends PrefabAuthenticator[Id[User], SecurityContext](config, authenticationDao) with Logging {

  def authenticateWithUsernamePassword(username: String, password: String): Future[Option[SecurityContext]] =
    partyDao.findUserByPrimaryEmail(username) flatMap {
      case Some((party, user)) =>
        credentialsDao.retrieveWithPassword(user.credentials, password) flatMap {
          case Some(credentials) =>
            setAuthenticated(user.id) map {
              authenticationId =>
                SecurityContext(party.id, user.id, authenticationId, Set.empty)
            }
          case None =>
             debug(s"User $username credentials do not match provided password ****")
             Future.successful(None)
        }
      case None =>  
        debug(s"Cannot authenticate username $username as cannot find any user associated with it.")
        Future.successful(None)
  }
}