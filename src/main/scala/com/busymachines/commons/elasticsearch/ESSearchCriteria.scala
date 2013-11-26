package com.busymachines.commons.elasticsearch

import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import com.busymachines.commons.dao.SearchCriteria
import spray.json.JsonWriter
import org.elasticsearch.index.query.QueryStringQueryBuilder
import com.busymachines.commons.domain.GeoPoint
import org.elasticsearch.common.unit.DistanceUnit

trait ESSearchCriteria[A] extends SearchCriteria[A] {
  def toFilter: FilterBuilder
  def and(other: ESSearchCriteria[A]) =
    ESSearchCriteria.And(this, other)
  def or(other: ESSearchCriteria[A]) =
    ESSearchCriteria.Or(this, other)
  def not(other: ESSearchCriteria[A]) =
    ESSearchCriteria.Not(other)
  def prepend[A0](path : Path[A0, A]) : ESSearchCriteria[A0]
}

object ESSearchCriteria {
  class Delegate[A](criteria: => ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = criteria.toFilter
    def prepend[A0](path : Path[A0, A]) = criteria.prepend(path)
  }

  case class All[A]() extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.matchAllFilter
    def prepend[A0](path : Path[A0, A]) = All[A0]()
  }
  
  case class Not[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def not(other: ESSearchCriteria[A]) =
      Not((children.toSeq :+ other): _*)
    def toFilter = FilterBuilders.notFilter(FilterBuilders.andFilter(children.map(_.toFilter): _*))
    def prepend[A0](path : Path[A0, A]) = Not(children.map(_.prepend(path)):_*)
  }

  case class And[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def and(other: ESSearchCriteria[A]) =
      And((children.toSeq :+ other): _*)
    def toFilter =
      children match {
        case Nil => FilterBuilders.matchAllFilter
        case f :: Nil => f.toFilter
        case _ => FilterBuilders.andFilter(children.map(_.toFilter): _*)
      }
    def prepend[A0](path : Path[A0, A]) = And(children.map(_.prepend(path)):_*)
  }

  case class Or[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def or(other: ESSearchCriteria[A]) =
      Or((children.toSeq :+ other): _*)
    def toFilter =
      children match {
        case Nil => FilterBuilders.matchAllFilter
        case f :: Nil => f.toFilter
        case _ => FilterBuilders.orFilter(children.map(_.toFilter): _*)
      }
    def prepend[A0](path : Path[A0, A]) = Or(children.map(_.prepend(path)):_*)
  }

  case class FGt[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value > doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FGt(path ++ path1, path ++ path2)
  }

  case class GeoDistance[A, T](path: Path[A, T], geoPoint: GeoPoint, radiusInKm: Double) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.geoDistanceFilter(path.toESPath).point(geoPoint.lat, geoPoint.lon).distance(radiusInKm, DistanceUnit.KILOMETERS)
    def prepend[A0](path : Path[A0, A]) = GeoDistance(path ++ this.path, geoPoint, radiusInKm)
  }

  case class Gt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).gt(value)
    def prepend[A0](path : Path[A0, A]) = Gt(path ++ this.path, value)
  }

  case class FGte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value >= doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FGte(path ++ path1, path ++ path2)
  }

  case class Gte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).gte(value)
    def prepend[A0](path : Path[A0, A]) = Gte(path ++ this.path, value)
  }

  case class FLt[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value < doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FLt(path ++ path1, path ++ path2)
  }

  case class Lt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).lt(value)
    def prepend[A0](path : Path[A0, A]) = Lt(path ++ this.path, value)
  }

  case class FLte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value <= doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FLte(path ++ path1, path ++ path2)
  }

  case class Lte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).lte(value)
    def prepend[A0](path : Path[A0, A]) = Lte(path ++ this.path, value)
  }

  case class FEq[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value == doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FEq(path ++ path1, path ++ path2)
  }

  case class Eq[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.termFilter(path.toESPath, value)
    def prepend[A0](path : Path[A0, A]) = Eq(path ++ this.path, value)
  }

  case class FNeq[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value != doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FNeq(path ++ path1, path ++ path2)
  }

  case class Neq[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = Not(Eq(path, value)).toFilter
    def prepend[A0](path : Path[A0, A]) = Neq(path ++ this.path, value)
  }

  case class In[A, T, V](path: Path[A, T], values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.inFilter(path.toESPath, values.map(v => v.toString): _*)
    def prepend[A0](path : Path[A0, A]) = In(path ++ this.path, values)
  }

  case class Missing[A, T](path: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.missingFilter(path.toESPath).existence(true)
    def prepend[A0](path : Path[A0, A]) = Missing(path ++ this.path)
  }

  def missing[A, T](path: Path[A, T]) = Missing(path)

  
  case class Exists[A, T](path: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.existsFilter(path.toESPath)
    def prepend[A0](path : Path[A0, A]) = Exists(path ++ this.path)
  }

  def exists[A, T](path: Path[A, T]) = Exists(path)
  
  case class Nested[A, T](path: Path[A, T])(criteria : ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.nestedFilter(path.toESPath, criteria.toFilter) 
    def prepend[A0](path : Path[A0, A]) = Nested(path ++ this.path)(criteria.asInstanceOf[ESSearchCriteria[A0]])
  }
}