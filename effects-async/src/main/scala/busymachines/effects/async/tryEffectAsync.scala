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
    implicit final def bmcTryAsyncCompanionObjectOps(obj: scala.util.Try.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcTryAsyncReferenceOps[T](value: Try[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcTryAsyncSafeReferenceOps[T](value: => Try[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: scala.util.Try.type) extends AnyVal {

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    @inline def asFuture[T](value: Try[T]): Future[T] =
      Future.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    @inline def asIO[T](value: Try[T]): IO[T] =
      IOOps.fromTry(value)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[Future#fromTry]]
      */
    @inline def suspendInFuture[T](value: => Try[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromTry]]
      */
    @inline def suspendInIO[T](value: => Try[T]): IO[T] =
      IOOps.suspendTry(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Try[T]) extends AnyVal {

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    @inline def asFuture: Future[T] =
      Future.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    @inline def asIO: IO[T] =
      IOOps.fromTry(value)
  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Try[T]) {

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[Future#fromTry]]
      */
    @inline def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromTry]]
      */
    @inline def suspendInIO: IO[T] =
      IOOps.suspendTry(value)
  }
}
