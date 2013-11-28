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

case class ESTermFacet(name: String, searchCriteria:SearchCriteria[_], fields: List[Path[_,_]], size: Int = 10, ordering: TermfacetOrders.Value = TermfacetOrders.termDesc) extends Facet