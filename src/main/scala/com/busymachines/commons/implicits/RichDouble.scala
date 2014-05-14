package com.busymachines.commons

/**
 * Created by ruud on 06/02/14.
 */
class RichDouble(val value : Double) extends AnyVal {

  def eq(other: Double, delta: Double) =
    Math.abs(value - other) < delta

  def lt(other: Double, delta: Double) =
    value + delta < other

  def gt(other: Double, delta: Double) =
    value - delta > other
}
