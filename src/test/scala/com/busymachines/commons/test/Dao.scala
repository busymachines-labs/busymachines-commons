package com.busymachines.commons.test
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import com.busymachines.commons.elasticsearch.EsRootDao
import com.busymachines.commons.elasticsearch.ESSearchCriteria
import com.busymachines.commons.elasticsearch.Index
import com.busymachines.commons.elasticsearch.Property.toPath
import com.busymachines.commons.elasticsearch.Type
import com.busymachines.commons.domain.Id
import com.busymachines.commons.test.DomainJsonFormats.itemFormat
import com.busymachines.commons.dao.DaoCache
import com.busymachines.commons.dao.DaoMutator
import scala.reflect.ClassTag
import com.busymachines.commons.dao.Dao
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.RootDaoMutator
import com.busymachines.commons.dao.RootDao

class ItemDao(index : Index)(implicit ec: ExecutionContext) extends EsRootDao[Item](index, Type("item", ItemMapping))
class ItemDaoMutator(dao:ItemDao)(implicit ec: ExecutionContext,classTag : ClassTag[Item]) extends RootDaoMutator[Item](dao)
