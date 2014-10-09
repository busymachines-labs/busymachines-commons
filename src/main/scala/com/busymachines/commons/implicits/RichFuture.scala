package com.busymachines.commons.implicits

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.collection.generic.CanBuildFrom
import scala.util.Try
import scala.util.Failure
import scala.concurrent.Promise

class RichFutureType(future: Future.type) {

  /**
   * Traverses given collection, executes corresponding futures SERIALLY
   * and returns a future of a collection of results.
   */
  @deprecated("Use RichIterable.traverse instead.", "1.0")
  def traverseSerial[A, B, C[A] <: Iterable[A]](collection: C[A])(fn: A => Future[B])(
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

class RichFuture[A](future: Future[A]) {
  def await(duration: Duration) = Await.result(future, duration)
  def await = Await.result(future, 1.minute)
  
  // Like flatMap, but also handles errors
  def chainWith[B](pf: PartialFunction[Try[A], Future[B]])(implicit executor: ExecutionContext): Future[B] = {
    val p = Promise[B]()
    future.onComplete { result =>
      pf(result).andThen { case result2 => p.complete(result2)} 
    }
    p.future
  }
} 
