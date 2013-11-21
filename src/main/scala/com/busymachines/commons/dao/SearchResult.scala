package com.busymachines.commons.dao

import com.busymachines.commons.domain.HasId

object SearchResult {
  implicit def toResult[T <: HasId[T]](result: SearchResult[T]) = result.result
}

case class SearchResult[T <: HasId[T]](result : List[Versioned[T]], totalCount : Option[Long] = None, facets : Map[String, List[FacetValue]] = Map.empty)