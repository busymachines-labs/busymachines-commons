package busymachines.result

import busymachines.core.Anomaly
import cats.effect.IO

import scala.concurrent.Future
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
final class ResultOps[T](private[this] val r: Result[T]) {

  def bimap[R](bad: Anomaly => Anomaly, good: T => R): Result[R] = {
    r.right.map(good).left.map(bad)
  }

  def morph[R](bad: Anomaly => R, good: T => R): R = r match {
    case Left(value)  => bad(value)
    case Right(value) => good(value)
  }

  def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => pf(a)
    case _ => r
  }

  def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => Result.pure(pf(a))
    case _ => r
  }

  //===========================================================================
  //===================== Result to various (pseudo)monads ====================
  //===========================================================================

  def asIO: IO[T] = r match {
    case Left(value)  => IO.raiseError(value.asThrowable)
    case Right(value) => IO.pure(value)
  }

  def unsafeAsOption: Option[T] = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => Option(value)
  }

  def asTry: Try[T] = r match {
    case Left(value)  => scala.util.Failure(value.asThrowable)
    case Right(value) => scala.util.Success(value)
  }

  def asFuture: Future[T] = r match {
    case Left(value)  => Future.failed(value.asThrowable)
    case Right(value) => Future.successful(value)
  }

  def unsafeGet: T = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => value
  }
}

final class SuspendedResultOps[T](r: => Result[T]) {

  /**
    * Use for those rare cases in which you suspect that functions returning Result
    * are not pure.
    *
    * Need for this is indicative of bugs in the functions you're calling
    *
    * Example usage:
    * {{{
    *   var sideEffect = 0
    *
    *   val suspendedSideEffect: IO[Int] = Result {
    *     println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    *     sideEffect = 42
    *     sideEffect
    *   }.suspendInIO
    *
    *  //this is not thrown:
    *  if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")
    * }}}
    */
  def suspendInIO: IO[T] = {
    IO(r).flatMap {
      case Right(value) => IO.pure(value)
      case Left(value)  => IO.raiseError(value.asThrowable)
    }
  }
}
