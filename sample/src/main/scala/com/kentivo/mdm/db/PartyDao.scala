package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext

import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.elasticsearch.EsRootDao
import com.kentivo.mdm.domain.DomainJsonFormats.partyFormat
import com.kentivo.mdm.domain.Party

class PartyDao(index : ESIndex)(implicit ec: ExecutionContext) extends EsRootDao[Party](index, ESType("party", PartyMapping)) 

