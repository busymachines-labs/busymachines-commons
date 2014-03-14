package com.busymachines.commons.elasticsearch

import com.busymachines.commons.dao.Facet
import com.busymachines.commons.dao.SearchCriteria

object TermfacetOrders extends Enumeration {
  type TermFacetOrder = Value
  val termAsc = Value("termAsc")
  val termDesc = Value("termDesc")
  val countAsc = Value("countAsc")
  val countDesc = Value("countDesc")
}

case class ESTermFacet(name: String, searchCriteria:SearchCriteria[_], fields: List[ESPath[_, _]], size: Int = 10, ordering: TermfacetOrders.Value = TermfacetOrders.termDesc) extends Facet

case class ESHistoryFacet(name:String, searchCriteria:SearchCriteria[_], keyScript:Option[String], valueScript:Option[String], interval:Option[Long]=None, size: Int = Int.MaxValue, ordering: TermfacetOrders.Value = TermfacetOrders.termDesc) extends Facet