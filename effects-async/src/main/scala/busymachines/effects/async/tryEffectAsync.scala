package busymachines.effects.async

import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
object TrySyntaxAsync {

  /**
    *
    */
  trait Implcits {
    implicit def bmcTryAsyncCompanionObjectOps(obj: Try.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcTryAsyncReferenceOps[T](value: Try[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcTryAsyncSafeReferenceOps[T](value: => Try[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Try.type) {

    def asFuture[T](value: Try[T]): Future[T] =
      Future.fromTry(value)

    def asIO[T](value: Try[T]): IO[T] =
      IOOps.fromTry(value)

    def asTask[T](value: Try[T]): Task[T] =
      Task.fromTry(value)

    def suspendInFuture[T](value: => Try[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    def suspendInIO[T](value: => Try[T]): IO[T] =
      IOOps.suspendTry(value)

    def suspendInTask[T](value: => Try[T]): Task[T] =
      ???
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Try[T]) {

    def asFuture: Future[T] =
      Future.fromTry(value)

    def asIO: IO[T] =
      IOOps.fromTry(value)

    def asTask: Task[T] =
      Task.fromTry(value)
  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Try[T]) {

    def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    def suspendInIO: IO[T] =
      IOOps.suspendTry(value)

    def suspendInTask: Task[T] =
      ???
  }
}
