package busymachines.future

import busymachines.core.Anomaly
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
  def syncUnsafeGet(timeout: FiniteDuration = duration.minutes(1)): T =
    FutureUtil.syncUnsafeGet(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def syncAwaitReady(timeout: FiniteDuration = duration.minutes(1)): Future[T] =
    FutureUtil.syncAwaitReady(f, timeout)

  /**
    * Using this is highly discouraged
    *
    * This is here more as a convenience method for testing
    */
  def syncAsResult(timeout: FiniteDuration = duration.minutes(1)): Result[T] =
    FutureUtil.syncAsResult(f, timeout)

  def asUnit(implicit ec: ExecutionContext): Future[Unit] = FutureUtil.asUnitFuture(f)
}

/**
  * This is the most useful thing since sliced-bread. No more hidden side-effects
  * from annoying Future.apply
  */
final class SafeFutureOps[T](f: => Future[T]) {

  def asIO(implicit ec: ExecutionContext): IO[T] = FutureUtil.asIO(f)
}

/**
  *
  */
object CompanionFutureOps {

  /**
    * @param t
    *   Never, ever use a side-effecting computation when defining the value of
    *   this parameter
    */
  def pure[T](t: T): Future[T] = FutureUtil.pure(t)

  def fail[T](a: Anomaly): Future[T] = FutureUtil.fail(a)

  def asIO[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] = FutureUtil.asIO(f)

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