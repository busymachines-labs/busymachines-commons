package com.busymachines.prefab.party.service

import scala.concurrent.Future
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.User

trait PartyService {
  
  def getParty(partyId: Id[Party])(implicit sc: SecurityContext): Future[Option[Party]]
  def listParties(implicit sc: SecurityContext): Future[List[Party]]
  def createParty(party: Party)(implicit sc: SecurityContext): Future[Party]
  def deleteParty(entityId: Id[Party])(implicit sc: SecurityContext): String
  def updateUser(id: Id[User], user : User)(implicit sc: SecurityContext): String
  def findUser(id: Id[User])(implicit sc: SecurityContext): Future[Option[User]]
}