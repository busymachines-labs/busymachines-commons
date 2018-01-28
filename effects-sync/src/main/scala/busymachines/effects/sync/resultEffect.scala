package busymachines.effects.sync

import busymachines.core._

import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
trait ResultTypeDefinitions {
  type Result[T]    = Either[Anomaly, T]
  type Correct[T]   = Right[Anomaly,  T]
  type Incorrect[T] = Left[Anomaly,   T]

  val Result: Either.type = Either
}

/**
  *
  */
object ResultSyntax {

  trait Implicits {

    implicit def bmcResultCompanionObjectOps(t: Result.type): ResultSyntax.CompanionObjectOps =
      new ResultSyntax.CompanionObjectOps(t)

    implicit def bmcResultReferenceOps[T](t: Result[T]): ResultSyntax.ReferenceOps[T] =
      new ResultSyntax.ReferenceOps(t)

    implicit def bmcResultNestedOptionOps[T](ropt: Result[Option[T]]): ResultSyntax.NestedOptionOps[T] =
      new ResultSyntax.NestedOptionOps(ropt)

    @scala.deprecated("Create an EitherEffect file, where all this stuff goes", "now")
    implicit def bmcResultEitherOps[L, R](eit: Either[L, R]): ResultSyntax.EitherOps[L, R] =
      new ResultSyntax.EitherOps(eit)

    implicit def bmcResultBooleanOps(b: Boolean): ResultSyntax.BooleanOps =
      new ResultSyntax.BooleanOps(b)

    implicit def bmcResultNestedBooleanOps(b: Result[Boolean]): ResultSyntax.NestedBooleanOps =
      new ResultSyntax.NestedBooleanOps(b)
  }

  /**
    *
    */
  final class CompanionObjectOps(val r: Result.type) extends AnyVal {
    //===========================================================================
    //========================== Primary constructors ===========================
    //===========================================================================

    def pure[T](t: T): Result[T] = ResultOps.pure(t)

    def correct[T](t: T): Result[T] = ResultOps.correct(t)

    def fail[T](a: Anomaly): Result[T] = ResultOps.fail(a)

    def incorrect[T](a: Anomaly): Result[T] = ResultOps.incorrect(a)

    def unit: Result[Unit] = ResultOps.unit

    def apply[T](thunk: => T): Result[T] =
      ResultOps.apply(thunk)

    //===========================================================================
    //==================== Result from various other effects ====================
    //===========================================================================

    def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] =
      ResultOps.fromEither(elr)

    def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] =
      ResultOps.fromEither(elr, transformLeft)

    def fromTry[T](t: Try[T]): Result[T] = ResultOps.fromTry(t)

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] =
      ResultOps.fromOption(opt, ifNone)

    //===========================================================================
    //==================== Result from special cased Result =====================
    //===========================================================================

    def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Result[T] =
      ResultOps.cond(test, correct, anomaly)

    def failOnTrue(test: Boolean, anomaly: => Anomaly): Result[Unit] =
      ResultOps.failOnTrue(test, anomaly)

    def failOnFalse(test: Boolean, anomaly: => Anomaly): Result[Unit] =
      ResultOps.failOnFalse(test, anomaly)

    def flatCond[T](test: Result[Boolean], correct: => T, anomaly: => Anomaly): Result[T] =
      ResultOps.flatCond(test, correct, anomaly)

    def flatFailOnTrue(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnTrue(test, anomaly)

    def flatFailOnFalse(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnFalse(test, anomaly)

    //===========================================================================
    //===================== Result to various (pseudo)monads ====================
    //===========================================================================

    def unsafeAsOption[T](r: Result[T]): Option[T] =
      ResultOps.unsafeAsOption(r)

    def unsafeAsList[T](r: Result[T]): List[T] =
      ResultOps.unsafeAsList(r)

    def asTry[T](r: Result[T]): Try[T] =
      ResultOps.asTry(r)

    def unsafeGet[T](r: Result[T]): T =
      ResultOps.unsafeGet(r)

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def bimap[T, R](r: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] =
      ResultOps.bimap(r, good, bad)

    def morph[T, R](r: Result[T], good: T => R, bad: Anomaly => R): Result[R] =
      ResultOps.morph(r, good, bad)

    def recover[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] =
      ResultOps.recover(r, pf)

    def recoverWith[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, Result[R]]): Result[R] =
      ResultOps.recoverWith(r, pf)

    def discardContent[T](r: Result[T]): Result[Unit] =
      ResultOps.discardContent(r)
  }

  /**
    *
    */
  final class ReferenceOps[T](val r: Result[T]) extends AnyVal {

    def bimap[R](good: T => R, bad: Anomaly => Anomaly): Result[R] = ResultOps.bimap(r, good, bad)

    def morph[R](good: T => R, bad: Anomaly => R): Result[R] = ResultOps.morph(r, good, bad)

    def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] = ResultOps.recover(r, pf)

    def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] = ResultOps.recoverWith(r, pf)

    def discardContent: Result[Unit] = ResultOps.discardContent(r)

    //===========================================================================
    //===================== Result to various (pseudo)monads ====================
    //===========================================================================

    def unsafeAsOption: Option[T] = ResultOps.unsafeAsOption(r)

    def unsafeAsList: List[T] = ResultOps.unsafeAsList(r)

    def asTry: Try[T] = ResultOps.asTry(r)

    def unsafeGet: T = ResultOps.unsafeGet(r)
  }

  /**
    *
    */
  final class NestedOptionOps[T](val ropt: Result[Option[T]]) extends AnyVal {

    def flattenOption(ifNone: => Anomaly): Result[T] = ResultOps.flattenOption(ropt, ifNone)

  }

  /**
    *
    */
  @scala.deprecated("Create an EitherEffect file, where all this stuff goes", "now")
  final class EitherOps[L, R](private[this] val eit: Either[L, R]) {

    @scala.deprecated("Create an EitherEffect file, where all this stuff goes", "now")
    def asResult(implicit ev: L <:< Throwable): Result[R] = ResultOps.fromEither(eit)

    @scala.deprecated("Create an EitherEffect file, where all this stuff goes", "now")
    def asResult(transformLeft: L => Anomaly): Result[R] = ResultOps.fromEither(eit, transformLeft)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val b: Boolean) {

    def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = ResultOps.cond(b, correct, anomaly)

    def failOnTrue(anomaly: => Anomaly): Result[Unit] = ResultOps.failOnTrue(b, anomaly)

    def failOnFalse(anomaly: => Anomaly): Result[Unit] = ResultOps.failOnFalse(b, anomaly)
  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val br: Result[Boolean]) {

    def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = ResultOps.flatCond(br, correct, anomaly)

    def failOnTrue(anomaly: => Anomaly): Result[Unit] = ResultOps.flatFailOnTrue(br, anomaly)

    def failOnFalse(anomaly: => Anomaly): Result[Unit] = ResultOps.flatFailOnFalse(br, anomaly)
  }

}

//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================

object ResultOps {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t:    T): Result[T] = Correct(t)
  def correct[T](t: T): Result[T] = Correct(t)

  def fail[T](a:      Anomaly): Result[T] = Incorrect(a)
  def incorrect[T](a: Anomaly): Result[T] = Incorrect(a)

  val unit: Result[Unit] = Correct(())

  def apply[T](thunk: => T): Result[T] = {
    try {
      ResultOps.pure(thunk)
    } catch {
      case a: Anomaly                  => ResultOps.incorrect(a)
      case t: Throwable if NonFatal(t) => ResultOps.incorrect(CatastrophicError(t))
    }
  }

  //===========================================================================
  //==================== Result from various (pseudo)monads ===================
  //===========================================================================

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => ResultOps.incorrect(a)
          case NonFatal(t) => ResultOps.incorrect(CatastrophicError(t))
        }
      case Right(value) => ResultOps.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = {
    elr match {
      case Left(left)   => ResultOps.incorrect(transformLeft(left))
      case Right(value) => ResultOps.pure(value)
    }
  }

  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => ResultOps.incorrect(a)
    case Failure(NonFatal(r)) => ResultOps.incorrect(CatastrophicError(r))
    case Success(value)       => ResultOps.pure(value)
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = {
    opt match {
      case None    => ResultOps.incorrect(ifNone)
      case Some(v) => ResultOps.pure(v)
    }
  }

  //===========================================================================
  //==================== Result from special cased Result =====================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Result[T] =
    Either.cond[Anomaly, T](test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (test) ResultOps.incorrect(anomaly) else ResultOps.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (!test) ResultOps.incorrect(anomaly) else ResultOps.unit

  def flatCond[T](test: Result[Boolean], correct: => T, anomaly: => Anomaly): Result[T] =
    test flatMap (b => ResultOps.cond(b, correct, anomaly))

  def flatFailOnTrue(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (b) ResultOps.incorrect(anomaly) else ResultOps.unit)

  def flatFailOnFalse(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (!b) ResultOps.incorrect(anomaly) else ResultOps.unit)

  def flattenOption[T](fopt: Result[Option[T]], ifNone: => Anomaly): Result[T] =
    fopt flatMap (opt => ResultOps.fromOption(opt, ifNone))

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
    case Left(value)  => ResultOps.pure(bad(value))
    case Right(value) => ResultOps.pure(good(value))
  }

  def recover[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => ResultOps.pure(pf(a))
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
