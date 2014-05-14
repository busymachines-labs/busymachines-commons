package com.busymachines.commons

/**
 * Created by Ruud Diterwich on 30/12/13.
 */
class RichOption[A](val option : Option[A]) extends AnyVal {
  def isEmptyOr(a: A) = option match {
    case Some(value) if value == a => true
    case Some(value) => false
    case None => true
  }

  def mapTo[B] = option.map(_.asInstanceOf[B])
}
