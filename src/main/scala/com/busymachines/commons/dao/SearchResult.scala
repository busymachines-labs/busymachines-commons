package com.busymachines.commons.dao

object SearchResult {
  implicit def toResult[T](result: SearchResult[T]) = result.result
}
object VersionedSearchResult {
  implicit def toResult[T](result: VersionedSearchResult[T]) = result.result
}

case class SearchResult[T](result : List[T], totalCount : Option[Long] = None, facets : Map[String, List[FacetValue]] = Map.empty)
case class VersionedSearchResult[T](result : List[Versioned[T]], totalCount : Option[Long] = None, facets : Map[String, List[FacetValue]] = Map.empty)