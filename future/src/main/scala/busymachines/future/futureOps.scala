package busymachines.future

import busymachines.duration
import busymachines.duration.FiniteDuration
import busymachines.result._
import cats.effect.IO

import scala.collection.generic.CanBuildFrom

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
  def unsafeGet(timeout: FiniteDuration = duration.minutes(1)): T =
    FutureUtil.unsafeGet(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def toResult(timeout: FiniteDuration = duration.minutes(1)): Result[T] =
    FutureUtil.toResult(f, timeout)

}

/**
  * This is the most useful thing since sliced-bread. No more hidden side-effects
  * from annoying Future.apply
  */
final class SafeFutureOps[T](f: => Future[T]) {

  def toIO(implicit ec: ExecutionContext): IO[T] = {
    FutureUtil.toIO(f)
  }
}

/**
  *
  */
object CompanionFutureOps {

  def toIO[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    FutureUtil.toIO(f)

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
  def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
    ec:  ExecutionContext
  ): Future[C[B]] = FutureUtil.serialize(col)(fn)
}
