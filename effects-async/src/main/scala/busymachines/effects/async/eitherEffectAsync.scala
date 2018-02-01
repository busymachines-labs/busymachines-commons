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
      FutureOps.fromEither(value, bad)

    def asFutureThr[L, R](value: Either[L, R], bad: L => Throwable): Future[R] =
      FutureOps.fromEitherThr(value, bad)

    def asFutureThr[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherThr(value)

    def asIO[L, R](value: Either[L, R], bad: L => Anomaly): IO[R] =
      IOOps.fromEither(value, bad)

    def asIOThr[L, R](value: Either[L, R], bad: L => Throwable): IO[R] =
      IOOps.fromEitherThr(value, bad)

    def asIOThr[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(value)(ev)

    def asTask[L, R](value: Either[L, R], bad: L => Anomaly): Task[R] =
      TaskOps.suspendEither(value, bad)

    def asTaskThr[L, R](value: Either[L, R], bad: L => Throwable): Task[R] =
      TaskOps.suspendEitherThr(value, bad)

    def asTaskThr[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.suspendEitherThr(value)(ev)

    def suspendInFuture[L, R](value: => Either[L, R], bad: L => Anomaly)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.suspendEither(value, bad)

    def suspendInFutureThr[L, R](value: => Either[L, R], bad: L => Throwable)(
      implicit ec: ExecutionContext
    ): Future[R] =
      FutureOps.suspendEitherThr(value, bad)

    def suspendInFutureThr[L, R](value: => Either[L, R])(
      implicit
      ev: L <:< Throwable,
      ec: ExecutionContext
    ): Future[R] = FutureOps.suspendEitherThr(value)(ev, ec)

    def suspendInIO[L, R](value: => Either[L, R], bad: L => Anomaly): IO[R] =
      IOOps.suspendEither(value, bad)

    def suspendInIOThr[L, R](value: => Either[L, R], bad: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(value, bad)

    def suspendInIOThr[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(value)(ev)

    def suspendInTask[L, R](value: => Either[L, R], bad: L => Anomaly): Task[R] =
      TaskOps.suspendEither(value, bad)

    def suspendInTaskThr[L, R](value: => Either[L, R], bad: L => Throwable): Task[R] =
      TaskOps.suspendEitherThr(value, bad)

    def suspendInTaskThr[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.suspendEitherThr(value)(ev)
  }

  /**
    *
    */
  final class ReferenceOps[L, R](private[this] val value: Either[L, R]) {

    def asFuture(bad: L => Anomaly): Future[R] =
      FutureOps.fromEither(value, bad)

    def asFutureThr(bad: L => Throwable): Future[R] =
      FutureOps.fromEitherThr(value, bad)

    def asFutureThr(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherThr(value)

    def asIO(bad: L => Anomaly): IO[R] =
      IOOps.fromEither(value, bad)

    def asIOThr(bad: L => Throwable): IO[R] =
      IOOps.fromEitherThr(value, bad)

    def asIOThr(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(value)(ev)

    def asTask(bad: L => Anomaly): Task[R] =
      TaskOps.fromEither(value, bad)

    def asTaskThr(bad: L => Throwable): Task[R] =
      TaskOps.fromEitherThr(value, bad)

    def asTaskThr(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.fromEitherThr(value)(ev)

  }

  /**
    *
    */
  final class SafeReferenceOps[L, R](value: => Either[L, R]) {

    def suspendInFuture(bad: L => Anomaly)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.suspendEither(value, bad)

    def suspendInFutureThr(bad: L => Throwable)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.suspendEitherThr(value, bad)

    def suspendInFutureThr(implicit ev: L <:< Throwable, ec: ExecutionContext): Future[R] =
      FutureOps.suspendEitherThr(value)

    def suspendInIO(bad: L => Anomaly): IO[R] =
      IOOps.suspendEither(value, bad)

    def suspendInIOThr(bad: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(value, bad)

    def suspendInIOThr(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(value)(ev)

    def suspendInTask(bad: L => Anomaly): Task[R] =
      TaskOps.suspendEither(value, bad)

    def suspendInTaskThr(bad: L => Throwable): Task[R] =
      TaskOps.suspendEitherThr(value, bad)

    def suspendInTaskThr(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.suspendEitherThr(value)(ev)

  }
}
