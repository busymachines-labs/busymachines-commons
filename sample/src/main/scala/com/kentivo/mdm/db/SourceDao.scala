package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext

import com.busymachines.commons.elasticsearch.EsRootDao
import com.busymachines.commons.elasticsearch.Index
import com.busymachines.commons.elasticsearch.Type
import com.kentivo.mdm.domain.DomainJsonFormats.sourceFormat
import com.kentivo.mdm.domain.Source

class SourceDao(index : Index)(implicit ec: ExecutionContext) extends EsRootDao[Source](index, Type("source", SourceMapping)) 

