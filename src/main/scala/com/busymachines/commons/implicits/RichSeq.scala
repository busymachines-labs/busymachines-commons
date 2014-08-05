package com.busymachines.commons.implicits

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.generic.CanBuildFrom

/**
 * To be moved to the more generic RichIterable.
 * @param seq
 * @tparam A
 */
class RichSeq[A, S[A] <: Seq[A]](val seq: S[A]) extends AnyVal {

  def duplicates: Seq[A] =
    seq.diff(seq.distinct)

  def modify(matches: A => Boolean, newA: => A, modify: A => A = (a: A) => a): List[A] =
    modifyFull(matches, newA, modify)._1
    
  def modifyFull(matches: A => Boolean, newA: => A, modify: A => A = (a: A) => a): (List[A], A, Boolean) = {
    var found: Option[A] = None
    var changed = false
    val newSeq = seq.toList.map {
      case a if matches(a) =>
        val modA = modify(a)
        found = Some(modA)
        changed = a != modA
        modA
      case a =>
        a
    }
    found match {
      case Some(a) =>
        (newSeq, a, changed)
      case None =>
        val modA = modify(newA)
        (newSeq :+ modA, modA, true)
    }
  }
}