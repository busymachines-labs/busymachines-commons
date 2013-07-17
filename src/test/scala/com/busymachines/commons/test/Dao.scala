package com.busymachines.commons.test
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import com.busymachines.commons.dao.elasticsearch.EsRootDao
import com.busymachines.commons.dao.elasticsearch.ESSearchCriteria
import com.busymachines.commons.dao.elasticsearch.Index
import com.busymachines.commons.dao.elasticsearch.Property.toPath
import com.busymachines.commons.dao.elasticsearch.Type
import com.busymachines.commons.domain.Id
import com.busymachines.commons.test.DomainJsonFormats.itemFormat

class ItemDao(index : Index)(implicit ec: ExecutionContext) extends EsRootDao[Item](index, Type("item", ItemMapping))