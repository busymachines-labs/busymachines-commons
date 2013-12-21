package com.busymachines.commons.concurrent

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.language.higherKinds

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