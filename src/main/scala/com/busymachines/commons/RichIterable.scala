package com.busymachines.commons

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.generic.CanBuildFrom

/**
 *
 * Created by Ruud Diterwich on 25-feb-2014.
 */
class RichIterable[A, C[A] <: Iterable[A]](val collection: C[A]) extends AnyVal {

  /**
   * Traverses the collection where each element is input an asynchronous function, resulting in a future
   * of a collection of results. Although the traverse is completely asynchronous, the traversal is done
   * serially, an element is processed only when the last element was done. This is different from the standard
   * Future.traverse implementation.
   */
  def traverse[B](fn: A => Future[B])(
    implicit ec: ExecutionContext,
    cbf: CanBuildFrom[C[B], B, C[B]]): Future[C[B]] = {

    val builder = cbf()
    builder.sizeHint(collection.size)

    collection.foldLeft(Future(builder)) {
      (previousFuture, next) =>
        for {
          previousResults <- previousFuture
          next <- fn(next)
        } yield previousResults += next
    } map { builder => builder.result() }
  }

  def mapTo[B] = collection.asInstanceOf[C[B]]
}
