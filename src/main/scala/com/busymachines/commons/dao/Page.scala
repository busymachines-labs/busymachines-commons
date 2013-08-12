package com.busymachines.commons.dao

object Page {
  val first = Page(0, 10)
  def first(size: Int) = Page(0, size)
}
case class Page(from: Int, size: Int)