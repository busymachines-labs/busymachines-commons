package com.busymachines.commons

class RichSeq[A](val seq : Seq[A]) extends AnyVal {
  
  def modify(matches : A => Boolean, newA: => A, modify: A => A = (a : A) => a): (List[A], A, Boolean) = {
    var found: Option[A] = None
    var changed = false
    val newSeq = seq.toList.map {
      case a if matches(a) =>
        val modA = modify(a)
        found = Some(modA);
        changed = a != modA
        newA
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