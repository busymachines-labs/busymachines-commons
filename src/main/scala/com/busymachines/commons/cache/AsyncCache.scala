package com.busymachines.commons.cache

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.Future
import scala.concurrent.Future.sequence
import spray.caching.{ Cache => SprayCache }
import spray.caching.ExpiringLruCache
import scala.concurrent.duration.Duration
import spray.caching.{Cache => SprayCache}
import spray.caching.{Cache => SprayCache}
import spray.caching.ValueMagnet.fromFuture

object AsyncCache {
  def expiringLru[K, V](initialCapacity: Int = 16, maxCapacity: Long = Long.MaxValue, timeToLive: Duration = Duration.Inf, timeToIdle: Duration = Duration.Inf) =
    new AsyncCache[K, V](new ExpiringLruCache[V](maxCapacity, initialCapacity, timeToLive, timeToIdle))
}

/**
 * Async
 * @param cache
 * @tparam K
 * @tparam V
 */
class AsyncCache[K, V](val cache: SprayCache[V]) {

  def apply(key: K) =
    cache.apply(key)

  def get(key: K): Option[Future[V]] =
    cache.get(key)

  def getOrElseUpdate(key: K, future: => Future[V])(implicit executor: ExecutionContext) =
    cache(key)(future)

  def setOrUpdate(key: K, future: => Future[V])(implicit executor: ExecutionContext):Future[V] =
    (cache.remove(key) match {
      case Some(f) => f
      case None => Future.successful()
    }) flatMap { _ => cache(key)(future) }

  def remove(key: K): Option[Future[V]] =
    cache.remove(key)

  def remove(keys: Set[K])(implicit executor: ExecutionContext): Map[K, Future[V]] = {
    keys.flatMap(key => cache.remove(key).map(key -> _)).toMap
  }

  def clear() =
    cache.clear()
}