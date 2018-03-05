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
    implicit final def bmcOptionAsyncCompanionObjectOps(obj: Option.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcOptionAsyncReferenceOps[T](value: Option[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcOptionAsyncSafeReferenceOps[T](value: => Option[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Option.type) extends AnyVal {

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asFuture[T](value: Option[T], ifNone: => Anomaly): Future[T] =
      FutureOps.fromOption(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect, if it is [[scala.None]]
      */
    @inline def asFutureThr[T](value: Option[T], ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asIO[T](value: Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asIOThr[T](value: Option[T], ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTask[T](value: Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.fromOption(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTaskThr[T](value: Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.fromOptionThr(value, ifNone)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendInFuture[T](value: => Option[T], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOption(value, ifNone)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendInFutureThr[T](value: => Option[T], ifNone: => Throwable)(
      implicit
      ec: ExecutionContext
    ): Future[T] =
      FutureOps.suspendOptionThr(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendInIO[T](value: => Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendInIOThr[T](value: => Option[T], ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendInTask[T](value: => Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendInTaskThr[T](value: => Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Option[T]) extends AnyVal {

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asFuture(ifNone: => Anomaly): Future[T] =
      FutureOps.fromOption(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect, if it is [[scala.None]]
      */
    @inline def asFutureThr(ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asIO(ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asIOThr(ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def asTask(ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def asTaskThr(ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)

    //=========================================================================
    //==================== Run side-effects on Option state ===================
    //=========================================================================

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[scala.None]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFailFuture(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFail(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[Some]]
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
      * Runs the given effect when the value of this [[Option]] is [[scala.None]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFailIO(effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

    /**
      * Runs the given effect when the value of this [[Option]] is [[Some]]
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
      * Runs the given effect when the value of this [[Option]] is [[scala.None]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFailTask(effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnFail(value, effect)

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[Some]]
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPureTask(effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnPure(value, effect)

  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Option[T]) {

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendInFuture(ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOption(value, ifNone)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendInFutureThr(ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOptionThr(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendInIO(ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendInIOThr(ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendInTask(ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(value, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendInTaskThr(ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(value, ifNone)
  }
}
