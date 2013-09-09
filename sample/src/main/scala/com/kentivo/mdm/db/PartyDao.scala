package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.CommonJsonFormats._
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESProperty.toPath
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.kentivo.mdm.domain.DomainJsonFormats.partyFormat
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.User
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned

class PartyDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Party](index, ESType("party", PartyMapping)) {
  
  def findByUserId(userId : Id[User]) = 
    searchSingle(PartyMapping.users / UserMapping.id equ userId)

  def findUserByPrimaryEmail(email : String) : Future[Option[(Party, User)]] = 
    searchSingle(PartyMapping.users / UserMapping.primaryEmail equ email) map { 
      case Some(Versioned(party, _)) =>
        party.users.find(_.primaryEmail == email).map((party, _))
      case _ => None
  }
} 

