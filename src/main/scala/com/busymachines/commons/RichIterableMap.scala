package com.busymachines.commons

import scala.collection.{Iterable, Map}

/**
 * Created by Ruud on 20/12/13.
 */
class RichIterableMap[K, V, I <: Iterable[V]](val map : Map[K, I]) extends AnyVal {

  def getFirst(key : K) : Option[V] =
    map.getOrElse(key, Nil).headOption

  def firstOrElse(key: K, default: => V) : V =
    getFirst(key).getOrElse(default)

}
