package busymachines.effects.async

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
      ???

    def asTask[T](value: Result[T]): Task[T] =
      ???

    def suspendInFuture[T](value: => Result[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    def suspendInIO[T](value: => Result[T]): IO[T] =
      ???

    def suspendInTask[T](value: => Result[T]): Task[T] =
      ???
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Result[T]) {

    def asFuture: Future[T] =
      FutureOps.fromResult(value)

    def asIO: IO[T] =
      ???

    def asTask: Task[T] =
      ???
  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Result[T]) {

    def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    def suspendInIO: IO[T] =
      ???

    def suspendInTask: Task[T] =
      ???
  }
}
