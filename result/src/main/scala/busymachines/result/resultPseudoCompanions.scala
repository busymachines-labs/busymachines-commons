package busymachines.result

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal
import busymachines.core._

/**
  * The reason I chose this instead of aliasing [[Either]] companion object
  * is to avoid situations where implicit ops defined on [[scala.util.Either#type]]
  * conflict with any operations I'd like to define here.
  *
  * Plus, I can seriously help out type inference by writing all method signatures in
  * terms of the rhs type only.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  */
object Result {
  def pure[T](t: T): Result[T] = Good(t)

  def fail[T](a: Anomaly): Result[T] = Bad(a)

  def unit: Result[Unit] = Good(())

  /**
    * Useful when one wants to do interop with unknown 3rd party code and you cannot
    * trust it not to throw exceptions in your face.
    */
  def apply[T](thunk: => T): Result[T] = {
    try {
      Result.pure(thunk)
    } catch {
      case a: Anomaly                  => Result.fail(a)
      case t: Throwable if NonFatal(t) => Result.fail(CatastrophicError(t))
    }
  }

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => Result fail a
          case NonFatal(t) => Result fail CatastrophicError(t)
        }
      case Right(value) => Result.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = {
    elr match {
      case Left(left)   => Result.fail(transformLeft(left))
      case Right(value) => Result.pure(value)
    }
  }

  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => Result.fail(a)
    case Failure(NonFatal(r)) => Result.fail(CatastrophicError(r))
    case Success(value) => Result.pure(value)
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = {
    opt match {
      case None    => Result.fail(ifNone)
      case Some(v) => Result.pure(v)
    }
  }

}



/**
  *
  *
  *
  *
  */
object Good {
  def apply[T](r: T): Result[T] = Right[Anomaly, T](r)

  def unapply[T](arg: Good[T]): Option[T] = Right.unapply(arg)
}

/**
  *
  *
  *
  *
  */
object Bad {
  def apply[T](a: Anomaly): Result[T] = Left[Anomaly, T](a)

  def unapply[T](arg: Bad[T]): Option[Anomaly] = Left.unapply(arg)
}
