package com.busymachines.commons.dao

object SearchResult {
  implicit def toResult[T](result: SearchResult[T]) = result.result
}

case class SearchResult[T](result : List[Versioned[T]], totalCount : Option[Long] = None, facets : Map[String, List[FacetValue]] = Map.empty, scroll:Option[Scroll]=None){
  def isScrollFinished=scroll match{
    case Some(a)=> result.size<a.size
    case None => true
  }
}