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
  def first(size: Int) = new Page(0, size)
  val all = new Page(0, 99999999)
}
case class Page(from: Int, size: Int)