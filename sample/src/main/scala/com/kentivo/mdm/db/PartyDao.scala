package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.CommonJsonFormats.idFormat
import com.busymachines.commons.domain.Id
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESProperty.toPath
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.kentivo.mdm.domain.DomainJsonFormats.partyFormat
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.Login

class PartyDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Party](index, ESType("party", PartyMapping)) {
  
  def findByUserId(userId : Id[User]) = 
    searchSingle(PartyMapping.users / UserMapping.id === userId)

  def findByLoginId(loginId : Id[Login]) = 
    searchSingle(PartyMapping.users / UserMapping.logins === loginId)
  
} 

