package com.busymachines.commons.elasticsearch

import org.elasticsearch.search.sort.SortOrder
import com.busymachines.commons.dao.SearchSort

object ESSearchSort {
  def none = Nil  
  def asc(field:String) = ESSearchSort(field,SortOrder.ASC)
  def desc(field:String) = ESSearchSort(field,SortOrder.DESC)
}

case class ESSearchSort(field:String,order:SortOrder) extends SearchSort {
  def asc:SearchSort= this.copy(order=SortOrder.ASC)
  def desc:SearchSort= this.copy(order=SortOrder.DESC)
}