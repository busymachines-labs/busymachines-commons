package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync.validated._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Feb 2018
  *
  */
object ValidatedSyntaxAsync {

  /**
    *
    */
  trait Implcits {
    implicit def bmcValidatedAsyncCompanionObjectOps(obj: Validated.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcValidatedAsyncReferenceOps[T](value: Validated[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcValidatedAsyncSafeReferenceOps[T](value: => Validated[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Validated.type) extends AnyVal {

    //
    def asFuture[T](value: Validated[T]): Future[T] =
      FutureOps.fromValidated(value)

    def asFuture[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
      FutureOps.fromValidated(value, ctor)

    def asIO[T](value: Validated[T]): IO[T] =
      IOOps.fromValidated(value)

    def asIO[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.fromValidated(value, ctor)

    def asTask[T](value: Validated[T]): Task[T] =
      TaskOps.fromValidated(value)

    def asTask[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.fromValidated(value, ctor)

    def suspendInFuture[T](value: => Validated[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value)

    def suspendInFuture[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies)(
      implicit
      ec: ExecutionContext
    ): Future[T] =
      FutureOps.suspendValidated(value, ctor)

    def suspendInIO[T](value: => Validated[T]): IO[T] =
      IOOps.suspendValidated(value)

    def suspendInIO[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.suspendValidated(value, ctor)

    def suspendInTask[T](value: => Validated[T]): Task[T] =
      TaskOps.suspendValidated(value)

    def suspendInTask[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.suspendValidated(value, ctor)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Validated[T]) extends AnyVal {

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asFuture: Future[T] =
      FutureOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asFuture(ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
      FutureOps.fromValidated(value, ctor)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asIO: IO[T] =
      IOOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asIO(ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.fromValidated(value, ctor)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asTask: Task[T] =
      TaskOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    def asTask(ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.fromValidated(value, ctor)
  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Validated[T]) {

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    def suspendInFuture(ctor: (Anomaly, List[Anomaly]) => Anomalies)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value, ctor)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[IOOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromValidated]]
      */
    def suspendInIO: IO[T] =
      IOOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[IOOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromValidated]]
      */
    def suspendInIO(ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.suspendValidated(value, ctor)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[TaskOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromValidated]]
      */
    def suspendInTask: Task[T] =
      TaskOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[TaskOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromValidated]]
      */
    def suspendInTask(ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.suspendValidated(value, ctor)
  }
}
