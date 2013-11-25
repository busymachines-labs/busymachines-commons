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
}

object ESSearchCriteria {
  class Delegate[A](criteria: => ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = criteria.toFilter
  }

  case class All[A]() extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.matchAllFilter
  }
  
  case class Not[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def not(other: ESSearchCriteria[A]) =
      Not((children.toSeq :+ other): _*)
    def toFilter = FilterBuilders.notFilter(FilterBuilders.andFilter(children.map(_.toFilter): _*))
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
  }

  case class FGt[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value > doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class GeoDistance[A, T](path: Path[A, T], geoPoint: GeoPoint, radiusInKm: Double) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.geoDistanceFilter(p.fieldName).point(geoPoint.lat, geoPoint.lon).distance(radiusInKm, DistanceUnit.KILOMETERS)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.geoDistanceFilter(names.mkString(".")).point(geoPoint.lat, geoPoint.lon).distance(radiusInKm, DistanceUnit.KILOMETERS))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Gt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.rangeFilter(p.fieldName).gt(value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).gt(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class FGte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value >= doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Gte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.rangeFilter(p.fieldName).gte(value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).gte(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class FLt[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value < doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Lt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.rangeFilter(p.fieldName).lt(value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).lt(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class FLte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value <= doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Lte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.rangeFilter(p.fieldName).lte(value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).lte(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class FEq[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value == doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Eq[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.termFilter(p.fieldName, value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.termFilter(names.mkString("."), value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Eq2[A, T](path: Path[A, T], value: T)(implicit writer: JsonWriter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.termFilter(p.fieldName, value)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.termFilter(names.mkString("."), value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class FNeq[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      (path1.toESPath, path2.toESPath) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value != doc['$p2Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Neq[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = Not(Eq(path, value)).toFilter
  }

  case class In[A, T, V](path: Path[A, T], values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.inFilter(p.fieldName, values.map(v => v.toString): _*)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.inFilter(names.mkString("."), values.map(v => v.toString): _*))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Missing[A, T](path: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.missingFilter(p.fieldName).existence(true)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.missingFilter(names.tail.mkString(".")).existence(true))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  def missing[A, T](path: Path[A, T]) = Missing(path)

  case class Exists[A, T](path: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.elements match {
        case p :: Nil => FilterBuilders.existsFilter(p.fieldName)
        case property :: rest =>
          val names = path.elements.map(_.fieldName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.existsFilter(names.tail.mkString(".")))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  def exists[A, T](path: Path[A, T]) = Exists(path)
  
}