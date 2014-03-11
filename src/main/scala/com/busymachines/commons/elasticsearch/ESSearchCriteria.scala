package com.busymachines.commons.elasticsearch

import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import com.busymachines.commons.dao.SearchCriteria
import spray.json.JsonWriter
import org.elasticsearch.index.query.QueryStringQueryBuilder
import com.busymachines.commons.domain.{Id, GeoPoint}
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

trait ESSearchCriteria[A] extends SearchCriteria[A] {
  def toFilter: FilterBuilder
  def and(other: ESSearchCriteria[A]) =
    ESSearchCriteria.And(this, other)
  def and(other: Option[ESSearchCriteria[A]]) =
    other match {
      case Some(c) => ESSearchCriteria.And(this, c) 
      case None => this 
    }
  def or(other: ESSearchCriteria[A]) =
    ESSearchCriteria.Or(this, other)
  def or(other: Option[ESSearchCriteria[A]]) =
    other match {
      case Some(c) => ESSearchCriteria.Or(this, c) 
      case None => this 
    }
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

  case class Range[A, T, V](path: Path[A, T], value: (V,V))(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).gt(value._1 match {
      case theValue:DateTime => theValue.toDateTime(DateTimeZone.UTC).getMillis()
      case theValue => theValue
    }).lt(value._2 match {
      case theValue:DateTime => theValue.getMillis()
      case theValue => theValue
    })
    
    def prepend[A0](path : Path[A0, A]) = Range(path ++ this.path, value)
  }

  case class QueryString[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.queryFilter(new QueryStringQueryBuilder(value match {
      case theValue:DateTime => s"${theValue.toDateTime(DateTimeZone.UTC).getMillis()}"
      case theValue => theValue.toString
    }).defaultField(path.toESPath))
    
    def prepend[A0](path : Path[A0, A]) = QueryString(path ++ this.path, value)
  }
  
  case class Gt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).gt(value match {
      case theValue:DateTime => theValue.toDateTime(DateTimeZone.UTC).getMillis()
      case theValue => theValue
    })
    
    def prepend[A0](path : Path[A0, A]) = Gt(path ++ this.path, value)
  }

  case class FGte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value >= doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FGte(path ++ path1, path ++ path2)
  }

  case class Gte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).gte(value match {
      case theValue:DateTime => theValue.toDateTime(DateTimeZone.UTC).getMillis()
      case theValue => theValue
    })
    
    def prepend[A0](path : Path[A0, A]) = Gte(path ++ this.path, value)
  }

  case class FLt[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value < doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FLt(path ++ path1, path ++ path2)
  }

  case class Lt[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).lt(value match {
      case theValue:DateTime => theValue.toDateTime(DateTimeZone.UTC).getMillis()
      case theValue => theValue
    })
    
    def prepend[A0](path : Path[A0, A]) = Lt(path ++ this.path, value)
  }

  case class FLte[A, T, V](path1: Path[A, T], path2: Path[A, T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.scriptFilter(s"doc['${path1.toESPath}'].value <= doc['${path2.toESPath}'].value")
    def prepend[A0](path : Path[A0, A]) = FLte(path ++ path1, path ++ path2)
  }

  case class Lte[A, T, V](path: Path[A, T], value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) extends ESSearchCriteria[A] {
    def toFilter = FilterBuilders.rangeFilter(path.toESPath).lte(value match {
      case theValue:DateTime => theValue.toDateTime(DateTimeZone.UTC).getMillis()
      case theValue => theValue
    })
    
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

case class PathElement[A, T] (
  fieldName : String,
  isNested : Boolean
)

case class Path[A, T](elements: List[PathElement[_, _]]) {
  def head: PathElement[A, _] = elements.head.asInstanceOf[PathElement[A, _]]
  def last: PathElement[_, T] = elements.head.asInstanceOf[PathElement[_, T]]
  def /[A2 <: T, V2](property: PathElement[A2, V2]) = Path[A, V2](elements :+ property)
  def /[A2 <: T, V2](fieldName : String) = Path[A, V2](elements :+ PathElement[A, V2](fieldName, false))

  def geo_distance(geoPoint: GeoPoint, distanceInKm: Double) = ESSearchCriteria.GeoDistance(this, geoPoint, distanceInKm)

  def equ(path: Path[A, T]) = nest(Nil, this, ESSearchCriteria.FEq(this, path))
  def equ[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Eq(this, value))
  def neq(path: Path[A, T]) = nest(Nil, this, ESSearchCriteria.FNeq(this, path))
  def neq[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Neq(this, value))

  def range[V](value: (V,V))(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Range(this, value))
  def queryString[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.QueryString(this, value))
  
  def gt(path: Path[A, T]) = nest(Nil, this, ESSearchCriteria.FGt(this, path))
  def gt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Gt(this, value))
  def gte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Gte(this, value))
  def lt[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Lt(this, value))
  def lte[V](value: V)(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.Lte(this, value))
  def in[V](values: Seq[V])(implicit writer: JsonWriter[V], jsConverter: JsValueConverter[T]) = nest(Nil, this, ESSearchCriteria.In(this, values))
  def missing = nest(Nil, this, ESSearchCriteria.missing(this))
  def exists = nest(Nil, this, ESSearchCriteria.exists(this))
  def ++[V] (other : Path[T, V]) = Path[A, V](elements ++ other.elements)

  /**
   * Nest given (usually compound) criteria inside a single nested filter.
   */
  def apply(criteria : ESSearchCriteria[T]) =
    ESSearchCriteria.Nested(this)(criteria.prepend(this))
  
  /**
   * Make sure that nested properties in given path create a nested filter in the resulting query.
   */
  private def nest[A0, A, T](prefix: List[PathElement[_, _]], path : Path[A, T], criteria : ESSearchCriteria[A]) : ESSearchCriteria[A] = {
    path.elements match {
      case head :: tail if head.isNested =>
        val untilNow = prefix ++ List(head)
        ESSearchCriteria.Nested(Path(untilNow))(nest(untilNow, Path(tail), criteria))
      case _ => criteria
    }
  }
  
  def toESPath =
    elements.map(_.fieldName).mkString(".")
}

