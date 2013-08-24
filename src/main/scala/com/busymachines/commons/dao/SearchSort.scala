package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId
import org.elasticsearch.search.sort.SortOrder

trait SearchSort {
  def field:String
  def order:SortOrder
}
