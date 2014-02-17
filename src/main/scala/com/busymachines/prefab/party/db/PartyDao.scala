package com.busymachines.prefab.party.db

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESProperty.toPath
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.User
import com.busymachines.commons.implicits._
import com.busymachines.prefab.party.implicits._

class PartyDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Party](index, ESType("party", PartyMapping)) {
  
  def findByUserId(userId : Id[User]) = 
    searchSingle(PartyMapping.users / UserMapping.id equ userId)

  def findByCredentialsId(credentialsId : Id[Credentials]) = 
    searchSingle(PartyMapping.users / UserMapping.credentials equ credentialsId)
    
  def findUserById(id : Id[User]) : Future[Option[(Party, User)]] = 
    searchSingle(PartyMapping.users / UserMapping.id equ id) map { 
      case Some(Versioned(party, _)) =>
        party.users.find(_.id == id).map((party, _))
      case _ => None
  }
  
  def findUserByCredentialsId(id : Id[Credentials]) : Future[Option[(Party, User)]] = 
    searchSingle(PartyMapping.users / UserMapping.credentials equ id) map { 
      case Some(Versioned(party, _)) =>
        party.users.find(_.credentials == id).map((party, _))
      case _ => None
  }
} 

