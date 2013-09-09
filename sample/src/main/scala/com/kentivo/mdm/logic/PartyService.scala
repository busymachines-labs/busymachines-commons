package com.kentivo.mdm.logic

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.Versioned
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.User
import spray.caching.LruCache
import com.kentivo.mdm.db.PartyDao
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import com.kentivo.mdm.db.UserDao
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials

class PartyService(partyDao: PartyDao, userDao : UserDao, credentialsDao : ESCredentialsDao, userAuthenticator : UserAuthenticator)(implicit ec: ExecutionContext) extends Logging {

  private val partyCache = LruCache[Option[Party]](2000, 50, 7 days, 8 hours)

  def createOrUpdateUserCredentials(userId: Id[User], newPassword: String): Future[Versioned[User]] =
    userDao.retrieve(userId) flatMap {
      case None => throw new Exception(s"Non existent user with id $userId")
      case Some(user) =>
        user.credentials map { credential =>
          credentialsDao.delete(credential) onFailure {
            case t => error("Cannot delete a credential", t)
          }
        }
        credentialsDao.create(Credentials(passwordCredentials = Some(PasswordCredentials(newPassword)))) flatMap { 
          storedCredentials =>
            userDao.modify(user.id)(_.copy(credentials = storedCredentials.id :: Nil))
        }
    }
  
  def list(implicit auth: SecurityContext): List[Party] = {
    Nil
  }

  /**
   * Create a party based on specific fields received.
   */
  def create(party: Party)(implicit auth: SecurityContext): Int = {
    0
  }

  /**
   * Find a specific party by id.
   */
  def getParty(partyId: Id[Party]): Future[Option[Party]] = {
    partyCache(partyId, () => partyDao.retrieve(partyId).map(_.map(_.entity)))
  }

  /**
   * Delete a specific party based on its id.
   */
  def delete(entityId: Id[Party]): String = {
    ""
  }

  /**
   * To check if user has enough rights to use a specific party id for specific operations (eg. to create a location for this partyId) we have to
   * check if that party is the party of current user OR if it's a child party.
   */
  def userHasEnoughRights(partyId: Id[Party], user: User) = {
    false
  }
}