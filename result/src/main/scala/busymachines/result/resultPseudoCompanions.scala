package busymachines.result

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal
import busymachines.core._

import scala.concurrent.Future

/**
  * The reason this approach was chosen instead of aliasing the [[Either]] companion object
  * is to avoid situations where implicit ops defined on [[scala.util.Either#type]]
  * conflict with any operations defined here.
  *
  * Plus, I can seriously help out type inference by writing all method signatures in
  * terms of the rhs type only.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  */
object Result {

  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t:    T): Result[T] = Correct(t)
  def correct[T](t: T): Result[T] = Correct(t)

  def fail[T](a:      Anomaly): Result[T] = Incorrect(a)
  def incorrect[T](a: Anomaly): Result[T] = Incorrect(a)

  def unit: Result[Unit] = Correct(())

  /**
    * Useful when one wants to do interop with unknown 3rd party code and you cannot
    * trust not to throw exceptions in your face
    */
  def apply[T](thunk: => T): Result[T] = {
    try {
      Result.pure(thunk)
    } catch {
      case a: Anomaly                  => Result.incorrect(a)
      case t: Throwable if NonFatal(t) => Result.incorrect(CatastrophicError(t))
    }
  }

  //===========================================================================
  //==================== Result from various (pseudo)monads ===================
  //===========================================================================

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => Result incorrect a
          case NonFatal(t) => Result incorrect CatastrophicError(t)
        }
      case Right(value) => Result.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = {
    elr match {
      case Left(left)   => Result.incorrect(transformLeft(left))
      case Right(value) => Result.pure(value)
    }
  }

  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => Result.incorrect(a)
    case Failure(NonFatal(r)) => Result.incorrect(CatastrophicError(r))
    case Success(value)       => Result.pure(value)
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = {
    opt match {
      case None    => Result.incorrect(ifNone)
      case Some(v) => Result.pure(v)
    }
  }

  //===========================================================================
  //==================== Result from special cased Result =====================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Result[T] =
    Either.cond[Anomaly, T](test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (test) Result.incorrect(anomaly) else Result.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (!test) Result.incorrect(anomaly) else Result.unit

  def flatCond[T](test: Result[Boolean], correct: => T, anomaly: => Anomaly): Result[T] =
    test flatMap (b => Result.cond(b, correct, anomaly))

  def flatFailOnTrue(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (b) Result.incorrect(anomaly) else Result.unit)

  def flatFailOnFalse(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (!b) Result.incorrect(anomaly) else Result.unit)

  //===========================================================================
  //===================== Result to various (pseudo)monads ====================
  //===========================================================================

  def unsafeAsOption[T](r: Result[T]): Option[T] = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => Option(value)
  }

  def unsafeAsList[T](r: Result[T]): List[T] = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => List(value)
  }

  def asTry[T](r: Result[T]): Try[T] = r match {
    case Left(value)  => scala.util.Failure(value.asThrowable)
    case Right(value) => scala.util.Success(value)
  }

  def asFuture[T](r: Result[T]): Future[T] = r match {
    case Left(value)  => Future.failed(value.asThrowable)
    case Right(value) => Future.successful(value)
  }

  def unsafeGet[T](r: Result[T]): T = r match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => value
  }

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](r: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] = {
    r.right.map(good).left.map(bad)
  }

  def morph[T, R](r: Result[T], good: T => R, bad: Anomaly => R): Result[R] = r match {
    case Left(value)  => Result.pure(bad(value))
    case Right(value) => Result.pure(good(value))
  }

  def recover[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => Result.pure(pf(a))
    case _ => r
  }

  def recoverWith[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, Result[R]]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => pf(a)
    case _ => r
  }

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](r: Result[T]): Result[Unit] = r.map(UnitFunction)
}

/**
  * Convenience methods to provide more semantically meaningful pattern matches.
  * If you want to preserve the semantically richer meaning of Result, you'd
  * have to explicitely match on the Left with Anomaly, like such:
  * {{{
  *   result match {
  *      Right(v)         => v //...
  *      Left(a: Anomaly) => throw a.asThrowable
  *   }
  * }}}
  *
  * But with these convenience unapplies, the above becomes:
  *
  * {{{
  *   result match {
  *      Correct(v)   => v //...
  *      Incorrect(a) =>  throw a.asThrowable
  *   }
  * }}}
  *
  */
object Correct {
  def apply[T](r: T): Result[T] = Right[Anomaly, T](r)

  def unapply[A <: Anomaly, C](arg: Right[A, C]): Option[C] = Right.unapply(arg)
}

object Incorrect {
  def apply[T](a: Anomaly): Result[T] = Left[Anomaly, T](a)

  def unapply[A <: Anomaly, C](arg: Left[A, C]): Option[A] = Left.unapply(arg)
}
