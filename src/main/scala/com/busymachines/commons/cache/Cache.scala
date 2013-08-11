package com.busymachines.commons.cache

import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.caching.{ Cache => SprayCache }
import spray.caching.ExpiringLruCache
import scala.concurrent.duration.Duration
import spray.caching.{Cache => SprayCache}
import spray.caching.{Cache => SprayCache}
import spray.caching.ValueMagnet.fromFuture

trait CacheWithCacheViews[K, V] {
  def invalidateCache(keys: K*): Future[Unit]
  def invalidateCacheViews(key: K, value: V): Future[Unit]
  def updateCacheViews(key: K, value: V): Future[Unit]
}

object Cache {
  def expiringLru[K, V](initialCapacity: Int, maxCapacity: Long = Long.MaxValue, timeToLive: Duration = Duration.Inf, timeToIdle: Duration = Duration.Inf) =
    new Cache[K, V](new ExpiringLruCache[V](maxCapacity, initialCapacity, if (timeToLive.isFinite) timeToLive.toMillis else 0, if (timeToIdle.isFinite) timeToIdle.toMillis else 0))
}

class Cache[K, V](val cache: SprayCache[V]) {

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

  def remove(keys: Set[K])(implicit executor: ExecutionContext): Future[Map[K, Option[V]]] = {
    Future.sequence(keys.map(key => cache.remove(key)).map(_ match {
      case None => Future.successful(None)
      case Some(f) => f
    })) map { results =>
      (keys zip (results)).map(t => t._1 ->
        (t._2.asInstanceOf[Option[V]] match {
          case None => None
          case Some(e) => Some(e)
        })) toMap
    }
  }

  def clear =
    cache.clear
}