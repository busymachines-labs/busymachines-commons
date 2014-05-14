package com.busymachines.commons.implicits

/**
 * Usefulness probably too specific to 'fields' structures, which will be replaced by extensions.
 * This class will go soon, or at least the applyUpdate.
 *
 * Created by Ruud on 20/12/13.
 */
class RichIterableMap[K, V, I <: Iterable[V]](val map : Map[K, I]) extends AnyVal {

  def getFirst(key : K) : Option[V] =
    map.getOrElse(key, Nil).headOption

  def firstOrElse(key: K, default: => V) : V =
    getFirst(key).getOrElse(default)

  def applyUpdate(update: Option[Map[K, I]]) : Map[K, I] = {
    update match {
      case Some(update) =>
        val (add, sub) = update.partition(_._2.nonEmpty)
        map ++ add -- sub.map(_._1)
      case None =>
        map
    }
  }

}
