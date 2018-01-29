package busymachines.effects.async

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
object EitherSyntaxAsync {

  /**
    *
    */
  trait Implcits {
    implicit def bmcEitherAsyncCompanionObjectOps(obj: Either.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcEitherAsyncReferenceOps[L, R](value: Either[L, R]): ReferenceOps[L, R] =
      new ReferenceOps(value)

    implicit def bmcEitherAsyncSafeReferenceOps[L, R](value: => Either[L, R]): SafeReferenceOps[L, R] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Either.type) {

    def asFuture[L, R](value: Either[L, R], bad: L => Anomaly): Future[R] =
      ???

    def asFutureWeak[L, R](value: Either[L, R], bad: L => Throwable): Future[R] =
      ???

    def asFutureWeak[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      ???

    def asIO[L, R](value: Either[L, R], bad: L => Anomaly): IO[R] =
      ???

    def asIOWeak[L, R](value: Either[L, R], bad: L => Throwable): IO[R] =
      ???

    def asIOWeak[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      ???

    def asTask[L, R](value: Either[L, R], bad: L => Anomaly): Task[R] =
      ???

    def asTaskWeak[L, R](value: Either[L, R], bad: L => Throwable): Task[R] =
      ???

    def asTaskWeak[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      ???

    def suspendInFuture[L, R](value: => Either[L, R], bad: L => Anomaly): Future[R] =
      ???

    def suspendInFutureWeak[L, R](value: => Either[L, R], bad: L => Throwable): Future[R] =
      ???

    def suspendInFutureWeak[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      ???

    def suspendInIO[L, R](value: => Either[L, R], bad: L => Anomaly): IO[R] =
      ???

    def suspendInIOWeak[L, R](value: => Either[L, R], bad: L => Throwable): IO[R] =
      ???

    def suspendInIOWeak[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      ???

    def suspendInTask[L, R](value: => Either[L, R], bad: L => Anomaly): Task[R] =
      ???

    def suspendInTaskWeak[L, R](value: => Either[L, R], bad: L => Throwable): Task[R] =
      ???

    def suspendInTaskWeak[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      ???
  }

  /**
    *
    */
  final class ReferenceOps[L, R](private[this] val value: Either[L, R]) {

    def asFuture(bad: L => Anomaly): Future[R] =
      ???

    def asFutureWeak(bad: L => Throwable): Future[R] =
      ???

    def asFutureWeak(implicit ev: L <:< Throwable): Future[R] =
      ???

    def asIO(bad: L => Anomaly): IO[R] =
      ???

    def asIOWeak(bad: L => Throwable): IO[R] =
      ???

    def asIOWeak(implicit ev: L <:< Throwable): IO[R] =
      ???

    def asTask(bad: L => Anomaly): Task[R] =
      ???

    def asTaskWeak(bad: L => Throwable): Task[R] =
      ???

    def asTaskWeak(implicit ev: L <:< Throwable): Task[R] =
      ???

  }

  /**
    *
    */
  final class SafeReferenceOps[L, R](value: => Either[L, R]) {

    def suspendInFuture(bad: L => Anomaly): Future[R] =
      ???

    def suspendInFutureWeak(bad: L => Throwable): Future[R] =
      ???

    def suspendInFutureWeak(implicit ev: L <:< Throwable): Future[R] =
      ???

    def suspendInIO(bad: L => Anomaly): IO[R] =
      ???

    def suspendInIOWeak(bad: L => Throwable): IO[R] =
      ???

    def suspendInIOWeak(implicit ev: L <:< Throwable): IO[R] =
      ???

    def suspendInTask(bad: L => Anomaly): Task[R] =
      ???

    def suspendInTaskWeak(bad: L => Throwable): Task[R] =
      ???

    def suspendInTaskWeak(implicit ev: L <:< Throwable): Task[R] =
      ???

  }
}
