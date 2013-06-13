package com.busymachines.commons

import com.typesafe.config.Config
import scala.math.Ordering.Implicits._
import scala.collection.JavaConversions._
import java.security.MessageDigest

class RichFunction[A, B](val f: A => Option[B]) extends PartialFunction[A, B] {
  private[this] var arg: Option[A] = None
  private[this] var result: Option[B] = None
  private[this] def cache(a: A) {
    if (Some(a) != arg) {
      arg = Some(a)
      result = f(a)
    }
  }
  def isDefinedAt(a: A) = {
    cache(a)
    result.isDefined
  }
  def apply(a: A) = {
    cache(a)
    result.get
  }
}