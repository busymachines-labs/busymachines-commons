package com.busymachines.prefab.party.service

import scala.concurrent.Future
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.authentication.model.Credentials

trait PartyService {

  /**
   * Creates or updates the credentials(loginName,password) for the given user
   * @param userId - the user ID
   * @param loginName - desired loginName
   * @param password - desired password
   * @return the new credentials
   */
  def setLoginNamePassword(userId: Id[User], loginName: String, password: String): Future[Credentials]

  /**
   * Retrieves a party by ID
   * @param partyId - the party ID
   * @param sc - implicit SecurityContext
   * @return
   */
  def getParty(partyId: Id[Party])(implicit sc: SecurityContext): Future[Option[Party]]

  /**
   *Returns a list of child parties for the given user
   * @param sc - implicit SecurityContext
   * @return - list of parties
   */
  def listChildParties(implicit sc: SecurityContext): Future[List[Party]]

  /**
   *Returns a list of all parties
   * @param sc - implicit SecurityContext
   * @return - list of parties
   */
  def listParties(implicit sc: SecurityContext): Future[List[Party]]

  /**
   *Stores a given party into database
   * @param party - the party to be stored
   * @param sc - implicit SecurityContext
   *@return - the stored party
   */
  def createParty(party: Party)(implicit sc: SecurityContext): Future[Party]

  /**
   *Deletes a party by ID
   * @param entityId - the party ID
   * @param sc - implicit SecurityContext
   * @return
   */
  def deleteParty(entityId: Id[Party])(implicit sc: SecurityContext): Future[Unit]

  /**
   *Updates a user by ID
   * @param id - the user ID
   * @param user - the updated user
   *@param sc - implicit SecurityContext
   *@return
   */
  def updateUser(id: Id[User], user: User)(implicit sc: SecurityContext): Future[Unit]

  /**
   * Retrieves a single party by email address
   * @param email - the email address
   * @param sc - implicit SecurityContext
   * @return - the party, if it exists
   */
  def getPartyByEmail(email:String)(implicit sc: SecurityContext): Future[Option[Party]]
  
  /**
   *Retrieves a user by ID
   * @param id - the user ID
   * @param sc - implicit SecurityContext
   * @return - the user, if it exists
   */
  def findUser(id: Id[User])(implicit sc: SecurityContext): Future[Option[User]]

  /**
   * To check if user has enough rights to use a specific party id for specific operations (eg. to create a location for this partyId) we have to
   * check if that party is the party of current user OR if it's a child party.
   * @param partyId - the party ID
   * @param user - the user ID
   * @return - true/false
   */
  def userHasEnoughRights(partyId: Id[Party], user: User): Future[Boolean]
}