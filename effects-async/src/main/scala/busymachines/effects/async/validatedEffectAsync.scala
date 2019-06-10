package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync.validated._

import cats.{data => cd}

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
    implicit final def bmcValidatedAsyncCompanionObjectOps(obj: cd.Validated.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcValidatedAsyncReferenceOps[T](value: Validated[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcValidatedAsyncSafeReferenceOps[T](value: => Validated[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: cd.Validated.type) extends AnyVal {

    //
    @inline def asFuture[T](value: Validated[T]): Future[T] =
      FutureOps.fromValidated(value)

    @inline def asFuture[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
      FutureOps.fromValidated(value, ctor)

    @inline def asIO[T](value: Validated[T]): IO[T] =
      IOOps.fromValidated(value)

    @inline def asIO[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.fromValidated(value, ctor)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask[T](value: Validated[T]): Task[T] =
      TaskOps.fromValidated(value)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.fromValidated(value, ctor)

    @inline def suspendInFuture[T](value: => Validated[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value)

    @inline def suspendInFuture[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies)(
      implicit
      ec: ExecutionContext,
    ): Future[T] =
      FutureOps.suspendValidated(value, ctor)

    @inline def suspendInIO[T](value: => Validated[T]): IO[T] =
      IOOps.suspendValidated(value)

    @inline def suspendInIO[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.suspendValidated(value, ctor)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask[T](value: => Validated[T]): Task[T] =
      TaskOps.suspendValidated(value)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
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
    @inline def asFuture: Future[T] =
      FutureOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    @inline def asFuture(ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
      FutureOps.fromValidated(value, ctor)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    @inline def asIO: IO[T] =
      IOOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    @inline def asIO(ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.fromValidated(value, ctor)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask: Task[T] =
      TaskOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask(ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
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
    @inline def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
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
    @inline def suspendInFuture(ctor: (Anomaly, List[Anomaly]) => Anomalies)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value, ctor)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[IOOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromValidated]]
      */
    @inline def suspendInIO: IO[T] =
      IOOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[IOOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromValidated]]
      */
    @inline def suspendInIO(ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.suspendValidated(value, ctor)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[TaskOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromValidated]]
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask: Task[T] =
      TaskOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      * Other than that it has the semantics of [[TaskOps.fromValidated]]
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromValidated]]
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask(ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.suspendValidated(value, ctor)
  }
}
