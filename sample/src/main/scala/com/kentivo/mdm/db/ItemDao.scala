package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.EsRootDao
import com.busymachines.commons.elasticsearch.ESSearchCriteria
import com.busymachines.commons.elasticsearch.Index
import com.busymachines.commons.elasticsearch.Property.toPath
import com.busymachines.commons.elasticsearch.Type
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.DomainJsonFormats.itemFormat
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property

case class HasValueForProperty(propertyId : Id[Property], value : Option[String] = None, locale : Option[Option[String]] = None, unit : Option[Unit] = None) extends ESSearchCriteria.Delegate (
  ItemMapping.values / PropertyValueMapping.property === propertyId.toString
)


class ItemDao(index : Index)(implicit ec: ExecutionContext) extends EsRootDao[Item](index, Type("item", ItemMapping))