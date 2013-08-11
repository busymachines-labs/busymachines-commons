package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESType
import com.kentivo.mdm.domain.DomainJsonFormats.sourceFormat
import com.kentivo.mdm.domain.Source

class SourceDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Source](index, ESType("source", SourceMapping)) 

