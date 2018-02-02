package busymachines.effects.async

import busymachines.core.Anomaly
import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
object ResultSyntaxAsync {

  /**
    *
    */
  trait Implcits {
    implicit def bmcResultAsyncCompanionObjectOps(obj: Result.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcResultAsyncReferenceOps[T](value: Result[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcResultAsyncSafeReferenceOps[T](value: => Result[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Result.type) {

    def asFuture[T](value: Result[T]): Future[T] =
      FutureOps.fromResult(value)

    def asIO[T](value: Result[T]): IO[T] =
      IOOps.fromResult(value)

    def asTask[T](value: Result[T]): Task[T] =
      TaskOps.fromResult(value)

    def suspendInFuture[T](value: => Result[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    def suspendInIO[T](value: => Result[T]): IO[T] =
      IOOps.suspendResult(value)

    def suspendInTask[T](value: => Result[T]): Task[T] =
      TaskOps.suspendResult(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Result[T]) {

    def asFuture: Future[T] =
      FutureOps.fromResult(value)

    def asIO: IO[T] =
      IOOps.fromResult(value)

    def asTask: Task[T] =
      TaskOps.fromResult(value)

    //=========================================================================
    //==================== Run side-effects on Option state ===================
    //=========================================================================

    def effectOnFailFuture[_](effect: Anomaly => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFail(value, effect)

    def effectOnPureFuture[_](effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnPure(value, effect)

    def effectOnFailIO[_](effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

    def effectOnPureIO[_](effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

    def effectOnFailTask[_](effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.effectOnFail(value, effect)

    def effectOnPureTask[_](effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnPure(value, effect)

  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Result[T]) {

    def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    def suspendInIO: IO[T] =
      IOOps.suspendResult(value)

    def suspendInTask: Task[T] =
      TaskOps.suspendResult(value)
  }
}
