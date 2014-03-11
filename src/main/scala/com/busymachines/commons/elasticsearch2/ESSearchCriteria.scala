package com.busymachines.commons.elasticsearch2

import com.busymachines.commons.domain.GeoPoint
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.index.query.{QueryStringQueryBuilder, FilterBuilder, FilterBuilders}
import com.busymachines.commons.dao.SearchCriteria

trait ESSearchCriteria[A] extends SearchCriteria[A] {

  import ESSearchCriteria._

  def and(other: ESSearchCriteria[A]): ESSearchCriteria[A] = And(Seq(this, other))
  def and(other: Option[ESSearchCriteria[A]]): ESSearchCriteria[A] = other.map(and).getOrElse(this)
  def or(other: ESSearchCriteria[A]): ESSearchCriteria[A] = Or(Seq(this, other))
  def or(other: Option[ESSearchCriteria[A]]): ESSearchCriteria[A] = other.map(or).getOrElse(this)
  def toFilter: FilterBuilder
  def prepend[A0](path: ESPath[A0, A]): ESSearchCriteria[A0]
}

object ESSearchCriteria {
  case class And[A](children: Seq[ESSearchCriteria[A]]) extends ESSearchCriteria[A] {
    override def and(other: ESSearchCriteria[A]) = And(children :+ other)
    def toFilter =
      children match {
        case Nil => FilterBuilders.matchAllFilter
        case f :: Nil => f.toFilter
        case _ => FilterBuilders.andFilter(children.map(_.toFilter): _*)
      }
    def prepend[A0](path: ESPath[A0, A]) = And(children.map(_.prepend(path)))
  }

  case class Or[A](children: Seq[ESSearchCriteria[A]]) extends ESSearchCriteria[A] {
    override def and(other: ESSearchCriteria[A]) = Or(children :+ other)
    def toFilter =
      children match {
        case Nil => FilterBuilders.matchAllFilter
        case f :: Nil => f.toFilter
        case _ => FilterBuilders.orFilter(children.map(_.toFilter): _*)
      }
    def prepend[A0](path: ESPath[A0, A]) = Or(children.map(_.prepend(path)))
  }

  case class Not[A](children: Seq[ESSearchCriteria[A]]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.notFilter(FilterBuilders.andFilter(children.map(_.toFilter): _*))
    def prepend[A0](path : ESPath[A0, A]) = Not(children.map(_.prepend(path)))
  }

  case class Equ[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.termFilter(path.toString, path.last.jsonFormat.write(value))
    def prepend[A0](path : ESPath[A0, A]) = Equ(path ++ this.path, value)
  }

  case class FEqu[A, T](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['$path1'].value == doc['$path2'].value")
    def prepend[A0](path : ESPath[A0, A]) = FEqu(path ++ path1, path ++ path2)
  }

  case class Neq[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = Not(Seq(Equ[A, T](path, value))).toFilter
    def prepend[A0](ESPath : ESPath[A0, A]) = Neq(ESPath ++ this.path, value)
  }

  case class FNeq[A, T](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toString}'].value != doc['${path2.toString}'].value")
    def prepend[A0](ESPath : ESPath[A0, A]) = FNeq(ESPath ++ path1, ESPath ++ path2)
  }

  case class Gt[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(ESPath.toString).gt(path.last.jsonFormat.write(value))
    def prepend[A0](ESPath : ESPath[A0, A]) = Gt(ESPath ++ this.path, value)
  }

  case class FGt[A, T](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toString}'].value > doc['${path2.toString}'].value")
    def prepend[A0](ESPath : ESPath[A0, A]) = FGt(ESPath ++ path1, ESPath ++ path2)
  }

  case class Gte[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(ESPath.toString).gte(path.last.jsonFormat.write(value))
    def prepend[A0](ESPath : ESPath[A0, A]) = Gte(ESPath ++ this.path, value)
  }

  case class FGte[A, T](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toString}'].value >= doc['${path2.toString}'].value")
    def prepend[A0](ESPath : ESPath[A0, A]) = FGte(ESPath ++ path1, ESPath ++ path2)
  }

  case class Lt[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(ESPath.toString).lt(path.last.jsonFormat.write(value))
    def prepend[A0](ESPath : ESPath[A0, A]) = Lt(ESPath ++ this.path, value)
  }

  case class FLt[A, T](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toString}'].value < doc['${path2.toString}'].value")
    def prepend[A0](ESPath : ESPath[A0, A]) = FLt(ESPath ++ path1, ESPath ++ path2)
  }

  case class Lte[A, T](path: ESPath[A, T], value: T) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(ESPath.toString).lte(path.last.jsonFormat.write(value))
    def prepend[A0](ESPath : ESPath[A0, A]) = Lte(ESPath ++ this.path, value)
  }

  case class FLte[A, T, V](path1: ESPath[A, T], path2: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toString}'].value <= doc['${path2.toString}'].value")
    def prepend[A0](ESPath : ESPath[A0, A]) = FLte(ESPath ++ path1, ESPath ++ path2)
  }

  case class In[A, T](path: ESPath[A, T], values: Seq[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.inFilter(ESPath.toString, values.map(path.last.jsonFormat.write): _*)
    def prepend[A0](ESPath : ESPath[A0, A]) = In(ESPath ++ this.path, values)
  }

  case class Missing[A, T](path: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.missingFilter(ESPath.toString).existence(true)
    def prepend[A0](ESPath : ESPath[A0, A]) = Missing(ESPath ++ this.path)
  }

  case class Exists[A, T](path: ESPath[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.existsFilter(path.toString)
    def prepend[A0](path : ESPath[A0, A]) = Exists(path ++ this.path)
  }

  case class GeoDistance[A, T](path: ESPath[A, T], geoPoint: GeoPoint, radiusInKm: Double) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.geoDistanceFilter(path.toString).point(geoPoint.lat, geoPoint.lon).distance(radiusInKm, DistanceUnit.KILOMETERS)
    def prepend[A0](path : ESPath[A0, A]) = GeoDistance(path ++ this.path, geoPoint, radiusInKm)
  }

  case class Range[A, T](path: ESPath[A, T], value: (T, T)) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toString).gt(path.last.jsonFormat.write(value._1)).lt(path.last.jsonFormat.write(value._2))
    def prepend[A0](path : ESPath[A0, A]) = Range(path ++ this.path, value)
  }

  case class Query[A, T](path: ESPath[A, T], query: String) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.queryFilter(new QueryStringQueryBuilder(query).defaultField(path.toString))
    def prepend[A0](path : ESPath[A0, A]) = Query(path ++ this.path, query)
  }

  case class Nested[A, T](path: ESPath[A, T])(criteria : ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.nestedFilter(path.toString, criteria.toFilter)
    def prepend[A0](path : ESPath[A0, A]) = Nested(path ++ this.path)(criteria.asInstanceOf[ESSearchCriteria[A0]])
  }

  def nest[A0, A, T](prefix: List[ESField[_, _]], path : ESPath[A, T], criteria : ESSearchCriteria[A]) : ESSearchCriteria[A] = {
    path.fields match {
      case head :: tail if head.isNested =>
        val untilNow = prefix ++ List(head)
        Nested[A, T](ESPath(untilNow))(nest(untilNow, ESPath(tail), criteria))
      case _ => criteria
    }
  }
}