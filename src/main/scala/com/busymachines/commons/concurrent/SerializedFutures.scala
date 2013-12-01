package com.busymachines.commons.concurrent

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq
import scala.concurrent.Promise
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

object SerializedFutures {

  /**
   * Traverses given collection, executes corresponding futures SERIALLY
   * and returns a future of a collection of results.
   */
  def traverse[A, B, C[A] <: Iterable[A]](collection: C[A])(fn: A => Future[B])(
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
    } map { builder => builder.result }
  }
}

/**
 * Makes sure that applied futures are executed serially.
 */
class SerializedFutures {

  private var _currentFuture: Future[Any] = Future.successful(null)

  def apply[A](future: => Future[A])(implicit ec: ExecutionContext): Future[A] =
    this.synchronized {
      val p = Promise[A]
      _currentFuture.onComplete { _ =>
        // by-name parameter future can throw an exception
        try {
          future.onComplete(p.complete)
        } catch {
          case t: Throwable => p.failure(t)
        }
      }
      val f = p.future
      _currentFuture = f
      f
    }
}