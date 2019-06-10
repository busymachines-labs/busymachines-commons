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
    implicit final def bmcResultAsyncCompanionObjectOps(obj: Result.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcResultAsyncReferenceOps[T](value: Result[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcResultAsyncSafeReferenceOps[T](value: => Result[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Result.type) extends AnyVal {

    @inline def asFuture[T](value: Result[T]): Future[T] =
      FutureOps.fromResult(value)

    @inline def asIO[T](value: Result[T]): IO[T] =
      IOOps.fromResult(value)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask[T](value: Result[T]): Task[T] =
      TaskOps.fromResult(value)

    @inline def suspendInFuture[T](value: => Result[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    @inline def suspendInIO[T](value: => Result[T]): IO[T] =
      IOOps.suspendResult(value)

    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask[T](value: => Result[T]): Task[T] =
      TaskOps.suspendResult(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Result[T]) extends AnyVal {

    /**
      *
      * Lift the [[busymachines.effects.sync.Result]] in this effect
      * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
      * [[busymachines.effects.sync.Correct]] becomes a pure effect
      *
      */
    @inline def asFuture: Future[T] =
      FutureOps.fromResult(value)

    /**
      *
      * Lift the [[busymachines.effects.sync.Result]] in this effect
      * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
      * [[busymachines.effects.sync.Correct]] becomes a pure effect
      *
      */
    @inline def asIO: IO[T] =
      IOOps.fromResult(value)

    /**
      *
      * Lift the [[busymachines.effects.sync.Result]] in this effect
      * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
      * [[busymachines.effects.sync.Correct]] becomes a pure effect
      *
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def asTask: Task[T] =
      TaskOps.fromResult(value)

    //=========================================================================
    //==================== Run side-effects on Option state ===================
    //=========================================================================

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFailFuture(effect: Anomaly => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFail(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPureFuture(effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnPure(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFailIO(effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPureIO(effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def effectOnFailTask(effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.effectOnFail(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def effectOnPureTask(effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnPure(value, effect)

  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Result[T]) {

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
      *
      * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromResult]]
      */
    @inline def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
      * Other than that it has the semantics of [[IOOps.fromResult]]
      *
      * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromResult]]
      */
    @inline def suspendInIO: IO[T] =
      IOOps.suspendResult(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
      * Other than that it has the semantics of [[TaskOps.fromResult]]
      *
      * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromResult]]
      */
    @scala.deprecated(
      "0.3.0-RC11",
      "Monix support will be dropped in 0.4.x — replace w/ cats-effect, or roll your own monix syntax",
    )
    @inline def suspendInTask: Task[T] =
      TaskOps.suspendResult(value)
  }
}
