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
    implicit final def bmcEitherAsyncCompanionObjectOps(obj: Either.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcEitherAsyncReferenceOps[L, R](value: Either[L, R]): ReferenceOps[L, R] =
      new ReferenceOps(value)

    implicit final def bmcEitherAsyncSafeReferenceOps[L, R](value: => Either[L, R]): SafeReferenceOps[L, R] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Either.type) extends AnyVal {

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asFuture[L, R](value: Either[L, R], bad: L => Anomaly): Future[R] =
      FutureOps.fromEither(value, bad)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asFutureThr[L, R](value: Either[L, R], bad: L => Throwable): Future[R] =
      FutureOps.fromEitherThr(value, bad)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
      * if it is a [[java.lang.Throwable]].
      */
    @inline def asFutureThr[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherThr(value)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asIO[L, R](value: Either[L, R], bad: L => Anomaly): IO[R] =
      IOOps.fromEither(value, bad)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asIOThr[L, R](value: Either[L, R], bad: L => Throwable): IO[R] =
      IOOps.fromEitherThr(value, bad)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
      * if it is a [[java.lang.Throwable]].
      */
    @inline def asIOThr[L, R](value: Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(value)(ev)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendInFuture[L, R](value: => Either[L, R], bad: L => Anomaly)(
      implicit ec:                           ExecutionContext,
    ): Future[R] =
      FutureOps.suspendEither(value, bad)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendInFutureThr[L, R](value: => Either[L, R], bad: L => Throwable)(
      implicit ec:                              ExecutionContext,
    ): Future[R] =
      FutureOps.suspendEitherThr(value, bad)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendInFutureThr[L, R](value: => Either[L, R])(
      implicit
      ev: L <:< Throwable,
      ec: ExecutionContext,
    ): Future[R] = FutureOps.suspendEitherThr(value)(ev, ec)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendInIO[L, R](value: => Either[L, R], bad: L => Anomaly): IO[R] =
      IOOps.suspendEither(value, bad)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendInIOThr[L, R](value: => Either[L, R], bad: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(value, bad)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendInIOThr[L, R](value: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(value)(ev)

  }

  /**
    *
    */
  final class ReferenceOps[L, R](val value: Either[L, R]) extends AnyVal {

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asFuture(bad: L => Anomaly): Future[R] =
      FutureOps.fromEither(value, bad)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asFutureThr(bad: L => Throwable): Future[R] =
      FutureOps.fromEitherThr(value, bad)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
      * if it is a [[java.lang.Throwable]].
      */
    @inline def asFutureThr(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherThr(value)

    @inline def asIO(bad: L => Anomaly): IO[R] =
      IOOps.fromEither(value, bad)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asIOThr(bad: L => Throwable): IO[R] =
      IOOps.fromEitherThr(value, bad)

    @inline def asIOThr(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(value)(ev)

  }

  /**
    *
    */
  final class SafeReferenceOps[L, R](value: => Either[L, R]) {

    @inline def suspendInFuture(bad: L => Anomaly)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.suspendEither(value, bad)

    @inline def suspendInFutureThr(bad: L => Throwable)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.suspendEitherThr(value, bad)

    @inline def suspendInFutureThr(implicit ev: L <:< Throwable, ec: ExecutionContext): Future[R] =
      FutureOps.suspendEitherThr(value)

    @inline def suspendInIO(bad: L => Anomaly): IO[R] =
      IOOps.suspendEither(value, bad)

    @inline def suspendInIOThr(bad: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(value, bad)

    @inline def suspendInIOThr(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(value)(ev)

  }
}
