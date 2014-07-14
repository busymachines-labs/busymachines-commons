package com.busymachines.prefab.party.db

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.party.domain.{PartyLocation, EmailAddress, Party, User}
import com.busymachines.commons.Implicits._
import com.busymachines.prefab.party.Implicits._

class PartyDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Party](index, ESType("party", PartyMapping)) {
 
  def findByFdcIdfNumber(idfNumber : String) = 
    searchSingle(Party.fdcIdfNumber equ idfNumber)

  def findByPartyName(partyName:String)= searchSingle(Party.company / CompanyMapping.name equ partyName)

  def findByUserId(userId : Id[User]) = 
    searchSingle(Party.users / User.id equ userId)
  
  def findByEmailId(email : String) = 
    searchSingle(Party.emailAddresses / EmailAddress.emailAddress equ email)

  def findByCredentialsId(credentialsId : Id[Credentials]) = 
    searchSingle(Party.users / User.credentials equ credentialsId)
    
  def findUserById(id : Id[User]) : Future[Option[(Party, User)]] = 
    searchSingle(Party.users / User.id equ id) map {
      case Some(Versioned(party, _)) =>
        party.users.find(_.id == id).map((party, _))
      case _ => None
  }
  def findByLocationId(id:Id[PartyLocation]): Future[Option[Versioned[Party]]]=searchSingle(Party.locations / PartyLocationMapping.id equ id)
  def findUserByCredentialsId(id : Id[Credentials]) : Future[Option[(Party, User)]] = 
    searchSingle(Party.users / User.credentials equ id) map {
      case Some(Versioned(party, _)) =>
        party.users.find(_.credentials == id).map((party, _))
      case _ => None
  }
} 

