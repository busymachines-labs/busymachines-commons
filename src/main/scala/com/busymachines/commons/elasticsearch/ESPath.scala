package com.busymachines.commons.elasticsearch

import com.busymachines.commons.Extension
import com.busymachines.commons.domain.GeoPoint
import scala.reflect.ClassTag
import spray.json.JsonFormat

trait ESPath[A, T] {

  import ESSearchCriteria._

  def fields: Seq[ESField[_, _]]
  def /[T2](field: ESField[T, T2]) = ESCompoundPath[A, T2](fields :+ field)
  def /[T2 :ClassTag :JsonFormat](field: AdhocField[T2]) = ESCompoundPath[A, T2](fields :+ field)
  def ++[T2] (other : ESPath[T, T2]) = ESCompoundPath[A, T2](fields ++ other.fields)

  def head = fields.head.asInstanceOf[ESField[A, _]]
  def last = fields.last.asInstanceOf[ESField[_, T]]

  def equ(value: T) = nest(Nil, this, Equ(this, value))
  def equ(path: ESPath[A, T]) = nest(Nil, this, FEqu(this, path))
  def neq(value: T) = nest(Nil, this, Neq(this, value))
  def neq(path: ESPath[A, T]) = nest(Nil, this, FNeq(this, path))
  def gt(value: T) = nest(Nil, this, Gt(this, value))
  def gt(path: ESPath[A, T]) = nest(Nil, this, FGt(this, path))
  def gte(value: T) = nest(Nil, this, Gte(this, value))
  def gte(path: ESPath[A, T]) = nest(Nil, this, FGte(this, path))
  def lt(value: T) = nest(Nil, this, Lt(this, value))
  def lt(path: ESPath[A, T]) = nest(Nil, this, FLt(this, path))
  def lte(value: T) = nest(Nil, this, Lte(this, value))
  def lte(path: ESPath[A, T]) = nest(Nil, this, FLte(this, path))
  def in(values: Seq[T]) = nest(Nil, this, In(this, values))
  def missing = nest(Nil, this, Missing(this))
  def exists = nest(Nil, this, Exists(this))

  def geo_distance(geoPoint: GeoPoint, radiusKm: Double) = nest(Nil, this, GeoDistance(this, geoPoint, radiusKm))
  def range(value: (T, T), radiusKm: Double) = nest(Nil, this, Range(this, value))
  def queryString(query: String) = nest(Nil, this, Query(this, query))

  /**
   * Nest given (usually compound) criteria inside a single nested filter.
   */
  def apply(criteria : ESSearchCriteria[T]) =
    ESSearchCriteria.Nested(this)(criteria.prepend(this))

  override def toString = fields.map(_.name).mkString(".")
}

object ESPath {
  def apply[A, T](fields: Seq[ESField[_, _]]) = ESCompoundPath[A, T](fields)
  implicit def fromExt[A, E, T](path: ESPath[E, T])(implicit e: Extension[A, E]) = path.asInstanceOf[ESPath[A, T]]
//  def apply[T2](field: String)(implicit ct: ClassTag[T2], fmt: JsonFormat[T2]) = ESField[Any, T2](field, "", Seq.empty, false, false, None)
//  implicit def toESField[A, T :ClassTag](field: String) = ESField[A, T](field, "", Seq.empty, false, false, None)(scala.reflect.classTag[T], null.asInstanceOf[JsonFormat[T]])
  //  def /[T2] (field: String)(implicit ct: ClassTag[T2], fmt: JsonFormat[T2]) = ESCompoundPath[A, T2](fields :+ ESField[Any, T2](field, "", Seq.empty, false, false, None))

}

case class ESCompoundPath[A, T](fields: Seq[ESField[_, _]]) extends ESPath[A, T]
