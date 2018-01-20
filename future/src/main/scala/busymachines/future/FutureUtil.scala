package busymachines.future

import busymachines.duration
import busymachines.duration.FiniteDuration
import busymachines.result._
import cats.effect.IO

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 20 Jan 2018
  *
  */
object FutureUtil {

  /**
    *
    * Syntactically inspired from [[Future.traverse]], but it differs semantically
    * insofar as this method does not attempt to run any futures in parallel.
    *
    * For the vast majority of cases you should prefer this method over [[Future.sequence]]
    * and [[Future.traverse]], since even small collections can easily wind up queuing so many
    * [[Future]]s that you blow your execution context.
    *
    * Usage:
    * {{{
    *   import busymachines.future._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: Future[Seq[Patch]] = Future.serialize(patches){ patch: Patch =>
    *     Future {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
    ec:  ExecutionContext
  ): Future[C[B]] = {
    if (col.isEmpty) {
      Future.successful(cbf.apply().result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      val firstBuilder = fn(head) map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Future[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Future[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder flatMap [mutable.Builder[B, C[B]]] { (result: mutable.Builder[B, C[B]]) =>
            val f: Future[mutable.Builder[B, C[B]]] = fn(element) map { newElement =>
              result.+=(newElement)
            }
            f
          }
      }
      eventualBuilder map { b =>
        b.result()
      }
    }
  }

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def unsafeGet[T](f: Future[T], timeout: FiniteDuration = duration.minutes(1)): T =
    Await.result(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def toResult[T](f: Future[T], timeout: FiniteDuration = duration.minutes(1)): Result[T] =
    Result(unsafeGet(f, timeout))

  /**
    * Safely suspend a Future in an IO monad, making this call referrentially
    * transparent, if the given future is yielded by a def or an uninitialized
    * lazy val.
    */
  def toIO[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(f))

}
