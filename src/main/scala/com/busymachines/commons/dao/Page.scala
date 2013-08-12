package com.busymachines.commons.dao

object Page {
  val first = Page(0, 10)
  def optional(from: Option[Int], size: Option[Int]) =
    (from, size) match {
      case (None, None) => first
      case (Some(f), None) => Page(f, 10)
      case (None, Some(s)) => Page(0, s)
      case (Some(f), Some(s)) => Page(f, s)
    }
  def first(size: Int) = Page(0, size)
  val all = Page(0, 99999999)
}
case class Page(from: Int, size: Int)