package com.busymachines.commons.dao

object Page {
  val first = new Page(0, 10)
  def apply(from: Option[Int], size: Option[Int]) =
    (from, size) match {
      case (None, None) => first
      case (Some(f), None) => new Page(f, 10)
      case (None, Some(s)) => new Page(0, s)
      case (Some(f), Some(s)) => new Page(f, s)
    }
  def apply(from: Option[Int], size: Option[Int], defaultPage:Page) =
    (from, size) match {
      case (None, None) => defaultPage
      case (Some(f), None) => new Page(f, 10)
      case (None, Some(s)) => new Page(0, s)
      case (Some(f), Some(s)) => new Page(f, s)
    }
  def first(size: Int) = new Page(0, size)

  // @deprecated("Use streaming when handling large data sets")
  val all = new Page(0, 9999)
  val none = new Page(0, 0)
}
case class Page(from: Int, size: Int)