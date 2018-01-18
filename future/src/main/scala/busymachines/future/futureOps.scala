package busymachines.future

import busymachines.duration
import busymachines.duration.FiniteDuration
import busymachines.result._
import cats.effect.IO

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/**
  *
  * These operations are impure, and should be rarely, if ever used. Definitely never in any
  * production code that responds to your request. Acceptable only on application startup, or test code
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
final class UnsafeFutureOps[T](private[this] val f: Future[T]) {

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def unsafeGet(timeout: FiniteDuration = duration.minutes(1)): T = Await.result(f, timeout)

  def asResult(timeout: FiniteDuration = duration.minutes(1)): Result[T] = Result(this.unsafeGet(timeout))

}

/**
  * This is the most useful thing since sliced-bread. No more hidden side-effects
  * from annoying Future.apply
  */
final class SafeFutureOps[T](f: => Future[T]) {

  def asIO(implicit ec: ExecutionContext): IO[T] = {
    IO.fromFuture(IO(f))
  }
}

/**
  *
  */
object CompanionFutureOps {

  def asIO[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] = {
    IO.fromFuture(IO(f))
  }

  /**
    *
    * Syntactically inspired from [[Future.traverse]], but it differs semantically
    * insofar as this method does not attempt to run any futures in parallel. "M" stands
    * for "monadic", as opposed to "applicative" which is the foundation for the formal definition
    * of "traverse" (even though in Scala it is by accident-ish)
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
  def traverseM[A, B, M[X] <: TraversableOnce[X]](traversable: M[A])(fn: A => concurrent.Future[B])(
    implicit
    cbf: CanBuildFrom[M[A], B, M[B]],
    ec:  ExecutionContext
  ): Future[M[B]] = {
    if (traversable.isEmpty) {
      Future.successful(cbf.apply().result())
    }
    else {
      val seq  = traversable.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, M[B]] = cbf.apply()
      val firstBuilder = fn(head) map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Future[mutable.Builder[B, M[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Future[mutable.Builder[B, M[B]]], element: A) =>
          serializedBuilder flatMap { (result: mutable.Builder[B, M[B]]) =>
            val f: Future[mutable.Builder[B, M[B]]] = fn(element) map { newElement =>
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
}
