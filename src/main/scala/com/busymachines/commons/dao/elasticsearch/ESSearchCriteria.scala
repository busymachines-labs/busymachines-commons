package com.busymachines.commons.dao.elasticsearch

import com.busymachines.commons.dao.SearchCriteria
import com.busymachines.commons.domain.HasId
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders

trait ESSearchCriteria[A] extends SearchCriteria[A] {
  def toFilter : FilterBuilder
  def && (other : ESSearchCriteria[A]) = 
    ESSearchCriteria.And(other)
}
  
object ESSearchCriteria {
  class Delegate[A](criteria : => ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = criteria.toFilter 
  }
  case class And[A](children : ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def && (other : ESSearchCriteria[A]) = 
      And((children.toSeq :+ other):_*)
    def toFilter = FilterBuilders.andFilter(children.map(_.toFilter):_*) 
  }
  case class Equals[A, T](path : Path[A, T], value : T) extends ESSearchCriteria[A] {
    def toFilter = 
      path.properties match {
        case p :: Nil => FilterBuilders.termFilter(p.mappedName, value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.termFilter(names.mkString("."), value))
        case _ => FilterBuilders.matchAllFilter
    }
  }
}