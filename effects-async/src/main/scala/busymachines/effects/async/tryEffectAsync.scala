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
    implicit def bmcTryAsyncCompanionObjectOps(obj: Try.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcTryAsyncReferenceOps[T](value: Try[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcTryAsyncSafeReferenceOps[T](value: => Try[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Try.type) {

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asFuture[T](value: Try[T]): Future[T] =
      Future.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asIO[T](value: Try[T]): IO[T] =
      IOOps.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asTask[T](value: Try[T]): Task[T] =
      Task.fromTry(value)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[Future.fromTry]]
      */
    def suspendInFuture[T](value: => Try[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromTry]]
      */
    def suspendInIO[T](value: => Try[T]): IO[T] =
      IOOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[Task.fromTry]]
      */
    def suspendInTask[T](value: => Try[T]): Task[T] =
      TaskOps.suspendTry(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Try[T]) {

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asFuture: Future[T] =
      Future.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asIO: IO[T] =
      IOOps.fromTry(value)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    def asTask: Task[T] =
      Task.fromTry(value)
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
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[Future.fromTry]]
      */
    def suspendInFuture(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromTry]]
      */
    def suspendInIO: IO[T] =
      IOOps.suspendTry(value)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[Task.fromTry]]
      */
    def suspendInTask: Task[T] =
      TaskOps.suspendTry(value)
  }
}
