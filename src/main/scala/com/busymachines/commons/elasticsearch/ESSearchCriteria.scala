package com.busymachines.commons.elasticsearch

import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import com.busymachines.commons.dao.SearchCriteria
import spray.json.JsonWriter
import org.elasticsearch.index.query.QueryStringQueryBuilder

trait ESSearchCriteria[A] extends SearchCriteria[A] {
  def toFilter: FilterBuilder
  def &&(other: ESSearchCriteria[A]) =
    ESSearchCriteria.And(other)
  def ||(other: ESSearchCriteria[A]) =
    ESSearchCriteria.Or(other)
  def not(other: ESSearchCriteria[A]) =
    ESSearchCriteria.Not(other)

}

object ESSearchCriteria {
  class Delegate[A](criteria: => ESSearchCriteria[A]) extends ESSearchCriteria[A] {
    def toFilter = criteria.toFilter
  }

  case class Not[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def not(other: ESSearchCriteria[A]) =
      Not((children.toSeq :+ other): _*)
    def toFilter = FilterBuilders.notFilter(FilterBuilders.andFilter(children.map(_.toFilter): _*))
  }

  case class And[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def &&(other: ESSearchCriteria[A]) =
      And((children.toSeq :+ other): _*)
    def toFilter = FilterBuilders.andFilter(children.map(_.toFilter): _*)
  }

  case class Or[A](children: ESSearchCriteria[A]*) extends ESSearchCriteria[A] {
    override def ||(other: ESSearchCriteria[A]) =
      Or((children.toSeq :+ other): _*)
    def toFilter = FilterBuilders.orFilter(children.map(_.toFilter): _*)
  }

  case class FGt[A, T, V](path1: Path[A, T], path2: Path[A, T])(implicit jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = {

      val p1 = path1.properties match {
        case p :: Nil => Some(p.mappedName)
        case property :: rest =>
          val names = path1.properties.map(_.mappedName)
          Some(names.mkString("."))
        case _ => None
      }

      val p2 = path2.properties match {
        case p :: Nil => Some(p.mappedName)
        case property :: rest =>
          val names = path2.properties.map(_.mappedName)
          Some(names.mkString("."))
        case _ => None
      }

      (p1, p2) match {
        case (Some(p1Field), Some(p2Field)) => FilterBuilders.scriptFilter(s"doc['$p1Field'].value &gt; doc['$p1Field'].value")
        case _ => FilterBuilders.matchAllFilter
      }
    }
  }

  case class Gt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.rangeFilter(p.mappedName).gt(value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).gt(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Gte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.rangeFilter(p.mappedName).gte(value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).gte(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Lt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.rangeFilter(p.mappedName).lt(value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).lt(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Lte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.rangeFilter(p.mappedName).lte(value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.rangeFilter(names.mkString(".")).lte(value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Equals[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.termFilter(p.mappedName, value)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.termFilter(names.mkString("."), value))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class In[A, T, V](path: Path[A, T], values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.inFilter(p.mappedName, values.map(v => v.toString): _*)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.inFilter(names.mkString("."), values.map(v => v.toString): _*))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  case class Missing[A, T](path: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter =
      path.properties match {
        case p :: Nil => FilterBuilders.missingFilter(p.mappedName)
        case property :: rest =>
          val names = path.properties.map(_.mappedName)
          FilterBuilders.nestedFilter(names.dropRight(1).mkString("."), FilterBuilders.missingFilter(names.mkString(".")))
        case _ => FilterBuilders.matchAllFilter
      }
  }

  def missing[A, T](path: Path[A, T]) = Missing(path)

}