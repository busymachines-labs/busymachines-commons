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
import com.kentivo.mdm.db.LoginDao
import com.kentivo.mdm.domain.Login

class PartyService(partyDao: PartyDao, loginDao: LoginDao)(implicit ec: ExecutionContext) {

  private val partyCache = LruCache[Option[Party]](2000, 50, 7 days, 8 hours)

  /**
   * Authenticate a user by its clear-text password.
   */
  def authenticate(email: String, password: String): Future[Option[(Party, User, Login)]] = {
    loginDao.findByEmail(email).flatMap {
      case Some(Versioned(login, _)) =>

        // check password
        if (login.withClearTextPassword(password).password == login.password) {

          // find corresponding party and user
          partyDao.findByLoginId(login.id).map {
            case Some(Versioned(party, _)) =>
              party.users.find(_.logins.exists(_.id == login.id)) match {
                case Some(user) => Some((party, user, login))
                case None => None
              }
            case None => None
          }
        } else Future.successful(None)
      case None => Future.successful(None)
    }
  }

  def list(implicit auth: AuthenticationData): List[Party] = {
    Nil
  }

  /**
   * Create a party based on specific fields received.
   */
  def create(party: Party)(implicit auth: AuthenticationData): Int = {
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