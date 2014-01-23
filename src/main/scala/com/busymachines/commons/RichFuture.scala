package com.busymachines.commons

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.collection.generic.CanBuildFrom

class RichFutureType(future: Future.type) {
//  def traverseSerialHack[A, B, C[A] <: Iterable[A]](collection: C[A])(fn: A => Future[B])(
//    implicit ec: ExecutionContext,
//    cbf: CanBuildFrom[C[B], B, C[B]]) : Future[C[B]] = {
//    val builder = cbf()
//    builder.sizeHint(collection.size)
//
//    collection.foreach(a => builder.+=(Await.result(fn(a), 2.minutes)))
//    Future.successful(builder.result)
//  }

  /**
   * Traverses given collection, executes corresponding futures SERIALLY
   * and returns a future of a collection of results.
   */
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
  def await[A](duration:Duration) = Await.result(future, duration)
  def await[A] = Await.result(future, 1.minute)
} 
