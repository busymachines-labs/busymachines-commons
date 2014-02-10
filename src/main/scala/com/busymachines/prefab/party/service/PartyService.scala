package com.busymachines.prefab.party.service

import scala.concurrent.Future
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.authentication.model.Credentials

trait PartyService {
  def setLoginNamePassword(userId: Id[User], loginName: String, password: String): Future[Credentials]
  def getParty(partyId: Id[Party])(implicit sc: SecurityContext): Future[Option[Party]]
  def listParties(implicit sc: SecurityContext): Future[List[Party]]
  def createParty(party: Party)(implicit sc: SecurityContext): Future[Party]
  def deleteParty(entityId: Id[Party])(implicit sc: SecurityContext): Future[Unit]
  def updateUser(id: Id[User], user: User)(implicit sc: SecurityContext): Future[Unit]
  def findUser(id: Id[User])(implicit sc: SecurityContext): Future[Option[User]]
}