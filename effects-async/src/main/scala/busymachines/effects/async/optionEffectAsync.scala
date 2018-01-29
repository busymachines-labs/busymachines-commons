package busymachines.effects.async

import busymachines.core._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
object OptionSyntaxAsync {

  /**
    *
    */
  trait Implcits {
    implicit def bmcOptionAsyncCompanionObjectOps(obj: Option.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcOptionAsyncReferenceOps[T](value: Option[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcOptionAsyncSafeReferenceOps[T](value: => Option[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Option.type) {

    def asFuture[T](value: Option[T], ifNone: => Anomaly): Future[T] =
      ???

    def asFutureWeak[T](value: Option[T], ifNone: => Throwable): Future[T] =
      ???

    def asIO[T](value: Option[T], ifNone: => Anomaly): IO[T] =
      ???

    def asIOWeak[T](value: Option[T], ifNone: => Throwable): IO[T] =
      ???

    def asTask[T](value: Option[T], ifNone: => Anomaly): Task[T] =
      ???

    def asTaskWeak[T](value: Option[T], ifNone: => Throwable): Task[T] =
      ???

    def suspendInFuture[T](value: => Option[T], ifNone: => Anomaly): Future[T] =
      ???

    def suspendInFutureWeak[T](value: => Option[T], ifNone: => Throwable): Future[T] =
      ???

    def suspendInIO[T](value: => Option[T], ifNone: => Anomaly): IO[T] =
      ???

    def suspendInIOWeak[T](value: => Option[T], ifNone: => Throwable): IO[T] =
      ???

    def suspendInTask[T](value: => Option[T], ifNone: => Anomaly): Task[T] =
      ???

    def suspendInTaskWeak[T](value: => Option[T], ifNone: => Throwable): Task[T] =
      ???
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Option[T]) {

    def asFuture(ifNone: => Anomaly): Future[T] =
      ???

    def asFutureWeak(ifNone: => Throwable): Future[T] =
      ???

    def asIO(ifNone: => Anomaly): IO[T] =
      ???

    def asIOWeak(ifNone: => Throwable): IO[T] =
      ???

    def asTask(ifNone: => Anomaly): Task[T] =
      ???

    def asTaskWeak(ifNone: => Throwable): Task[T] =
      ???

    //=========================================================================
    //==================== Run side-effects on Option state ===================
    //=========================================================================

    def effectOnFalseFuture[_](effect: => Future[_]): Future[Unit] =
      ???

    def effectOnTrueFuture[_](effect: => Future[_]): Future[Unit] =
      ???

    def effectOnFalseIO[_](effect: => IO[_]): IO[Unit] =
      ???

    def effectOnTrueIO[_](effect: => IO[_]): IO[Unit] =
      ???

    def effectOnFalseTask[_](effect: => Task[_]): Task[Unit] =
      ???

    def effectOnTrueTask[_](effect: => Task[_]): Task[Unit] =
      ???

  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Option[T]) {

    def suspendInFuture(ifNone: => Anomaly): Future[T] =
      ???

    def suspendInFutureWeak(ifNone: => Throwable): Future[T] =
      ???

    def suspendInIO(ifNone: => Anomaly): IO[T] =
      ???

    def suspendInIOWeak(ifNone: => Throwable): IO[T] =
      ???

    def suspendInTask(ifNone: => Anomaly): Task[T] =
      ???

    def suspendInTaskWeak(ifNone: => Throwable): Task[T] =
      ???
  }
}
