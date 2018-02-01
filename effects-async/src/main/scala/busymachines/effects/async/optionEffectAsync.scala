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
      FutureOps.fromOption(value, ifNone)

    def asFutureThr[T](value: Option[T], ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionThr(value, ifNone)

    def asIO[T](value: Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(value, ifNone)

    def asIOThr[T](value: Option[T], ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(value, ifNone)

    def asTask[T](value: Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.fromOption(value, ifNone)

    def asTaskThr[T](value: Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.fromOptionThr(value, ifNone)

    def suspendInFuture[T](value: => Option[T], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOption(value, ifNone)

    def suspendInFutureThr[T](value: => Option[T], ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOptionThr(value, ifNone)

    def suspendInIO[T](value: => Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(value, ifNone)

    def suspendInIOThr[T](value: => Option[T], ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(value, ifNone)

    def suspendInTask[T](value: => Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    def suspendInTaskThr[T](value: => Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Option[T]) {

    def asFuture(ifNone: => Anomaly): Future[T] =
      FutureOps.fromOption(value, ifNone)

    def asFutureThr(ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionThr(value, ifNone)

    def asIO(ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(value, ifNone)

    def asIOThr(ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(value, ifNone)

    def asTask(ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    def asTaskThr(ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)

    //=========================================================================
    //==================== Run side-effects on Option state ===================
    //=========================================================================

    def effectOnEmptyFuture[_](effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnEmpty(value, effect)

    def effectOnSomeFuture[_](effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnSome(value, effect)

    def effectOnEmptyIO[_](effect: => IO[_]): IO[Unit] =
      IOOps.effectOnEmpty(value, effect)

    def effectOnSomeIO[_](effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnSome(value, effect)

    def effectOnEmptyTask[_](effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnEmpty(value, effect)

    def effectOnSomeTask[_](effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnSome(value, effect)

  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Option[T]) {

    def suspendInFuture(ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOption(value, ifNone)

    def suspendInFutureThr(ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOptionThr(value, ifNone)

    def suspendInIO(ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(value, ifNone)

    def suspendInIOThr(ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(value, ifNone)

    def suspendInTask(ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    def suspendInTaskThr(ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)
  }
}
