//package com.busymachines.commons.elasticsearch2
//
//import org.elasticsearch.index.query.FilterBuilder
//import org.elasticsearch.index.query.FilterBuilders
//
//trait ESCriteria[A] {
//  import ESCriteria._
//  def and(other: ESCriteria[A]) = And(Seq(this, other))
//  protected def toFilter: FilterBuilder
//  protected def prepend[A0](path: ESPath[A0, A]): ESCriteria[A0]
//}
//
//object ESCriteria {
//  case class And[A](children: Seq[ESCriteria[A]]) extends ESCriteria[A] {
//    override def and(other: ESCriteria[A]) = And(children :+ other)
//    protected def toFilter =
//      children match {
//        case Nil => FilterBuilders.matchAllFilter
//        case f :: Nil => f.toFilter
//        case _ => FilterBuilders.andFilter(children.map(_.toFilter): _*)
//      }
//    protected def prepend[A0](path: ESPath[A0, A]) = And(children.map(_.prepend(path)))
//  }
//
//  case class Equ[A, T](path: ESPath[A, T], value: T) extends ESCriteria[A] {
//    protected def toFilter = FilterBuilders.termFilter(path.toString, value)
//    def prepend[A0](path : ESPath[A0, A]) = Equ(path ++ this.path, value)
//  }
//
//}