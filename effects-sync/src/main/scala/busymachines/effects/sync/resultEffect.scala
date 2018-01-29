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

object ResultSyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcResultCompanionObjectOps(obj: Result.type): CompationObjectOps =
      new CompationObjectOps(obj)

    implicit def bmcResultReferenceOps[T](value: Result[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcResultNestedOptionOps[T](nopt: Result[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit def bmcResultBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcResultNestedBooleanOps(test: Result[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompationObjectOps(val obj: Result.type) {

    //===========================================================================
    //========================== Primary constructors ===========================
    //===========================================================================

    def pure[T](value: T): Result[T] =
      ResultOps.pure(value)

    def fail[T](bad: Anomaly): Result[T] =
      ResultOps.fail(bad)

    def unit: Result[Unit] = ResultOps.unit

    def correct[T](t: T): Result[T] =
      ResultOps.correct(t)

    def incorrect[T](a: Anomaly): Result[T] =
      ResultOps.incorrect(a)

    def apply[T](thunk: => T): Result[T] =
      ResultOps.apply(thunk)

    //===========================================================================
    //==================== Result from various other effects ====================
    //===========================================================================

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] =
      ResultOps.fromOption(opt, ifNone)

    def fromTry[T](value: Try[T]) =
      ResultOps.fromTry(value)

    def fromEither[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Result[R] =
      ResultOps.fromEither(either)(ev)

    def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Result[R] =
      ResultOps.fromEither(either, transformLeft)

    /**
      * Similar to [[Either.cond]], but specialized on result. It has a different
      * name because the [[Either.cond]] method is always selected over this one.
      */
    def condResult[T](test: Boolean, good: => T, bad: => Anomaly): Result[T] =
      ResultOps.cond(test, good, bad)

    def condWith[T](test: Boolean, good: => Result[T], bad: => Anomaly): Result[T] =
      ResultOps.condWith(test, good, bad)

    def flatCond[T](test: Result[Boolean], good: => T, bad: => Anomaly): Result[T] =
      ResultOps.flatCond(test, good, bad)

    def flatCondWith[T](test: Result[Boolean], good: => Result[T], bad: => Anomaly): Result[T] =
      ResultOps.flatCondWith(test, good, bad)

    def failOnTrue(test: Boolean, bad: => Anomaly): Result[Unit] =
      ResultOps.failOnTrue(test, bad)

    def failOnFalse(test: Boolean, bad: => Anomaly): Result[Unit] =
      ResultOps.failOnFalse(test, bad)

    def flatFailOnTrue(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnTrue(test, bad)

    def flatFailOnFalse(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnFalse(test, bad)

    def flattenOption[T](nopt: Result[Option[T]], ifNone: => Anomaly): Result[T] =
      ResultOps.flattenOption(nopt, ifNone)

    def asOptionUnsafe[T](value: Result[T]): Option[T] =
      ResultOps.asOptionUnsafe(value)

    def asListUnsafe[T](value: Result[T]): List[T] =
      ResultOps.asListUnsafe(value)

    def asTry[T](value: Result[T]): Try[T] =
      ResultOps.asTry(value)

    def unsafeGet[T](value: Result[T]): T =
      ResultOps.unsafeGet(value)

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def bimap[T, R](value: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] =
      ResultOps.bimap(value, good, bad)

    def morph[T, R](value: Result[T], good: T => R, bad: Anomaly => R): Result[R] =
      ResultOps.morph(value, good, bad)

    def discardContent[T](value: Result[T]) =
      ResultOps.discardContent(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Result[T]) {

    def asOptionUnsafe(): Option[T] =
      ResultOps.asOptionUnsafe(value)

    def asListUnsafe(): List[T] =
      ResultOps.asListUnsafe(value)

    def asTry: Try[T] =
      ResultOps.asTry(value)

    def unsafeGet(): T =
      ResultOps.unsafeGet(value)

    def bimap[R](good: T => R, bad: Anomaly => Anomaly): Result[R] =
      ResultOps.bimap(value, good, bad)

    def morph[R](good: T => R, bad: Anomaly => R): Result[R] =
      ResultOps.morph(value, good, bad)

    def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] =
      ResultOps.recover(value, pf)

    def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] =
      ResultOps.recoverWith(value, pf)

    def discardContent: Result[Unit] =
      ResultOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val nopt: Result[Option[T]]) {
    def flattenOption(ifNone: => Anomaly): Result[T] = ResultOps.flattenOption(nopt, ifNone)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condResult[T](good: => T, bad: => Anomaly): Result[T] =
      ResultOps.cond(test, good, bad)

    def condWithResult[T](good: => Result[T], bad: => Anomaly): Result[T] =
      ResultOps.condWith(test, good, bad)

    def failOnTrueResult(bad: => Anomaly): Result[Unit] =
      ResultOps.failOnTrue(test, bad)

    def failOnFalseResult(bad: => Anomaly): Result[Unit] =
      ResultOps.failOnFalse(test, bad)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Result[Boolean]) {

    def cond[T](good: => T, bad: => Anomaly): Result[T] =
      ResultOps.flatCond(test, good, bad)

    def condWith[T](good: => Result[T], bad: => Anomaly): Result[T] =
      ResultOps.flatCondWith(test, good, bad)

    def failOnTrue(bad: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnTrue(test, bad)

    def failOnFalse(bad: => Anomaly): Result[Unit] =
      ResultOps.flatFailOnFalse(test, bad)

  }
}

//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================

/**
  *
  */
object ResultOps {

  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t:   T):       Result[T] = Correct(t)
  def fail[T](bad: Anomaly): Result[T] = Incorrect(bad)

  def correct[T](t:     T):       Result[T] = Correct(t)
  def incorrect[T](bad: Anomaly): Result[T] = Incorrect(bad)

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
  //==================== Result from various other effects =======================
  //===========================================================================

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = opt match {
    case None    => ResultOps.incorrect(ifNone)
    case Some(v) => ResultOps.pure(v)
  }

  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => ResultOps.incorrect(a)
    case Failure(NonFatal(r)) => ResultOps.incorrect(CatastrophicError(r))
    case Success(value)       => ResultOps.pure(value)
  }

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = elr match {
    case Left(left) =>
      ev(left) match {
        case a: Anomaly => ResultOps.incorrect(a)
        case NonFatal(t) => ResultOps.incorrect(CatastrophicError(t))
      }
    case Right(value) => ResultOps.pure(value)
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = elr match {
    case Left(left)   => ResultOps.incorrect(transformLeft(left))
    case Right(value) => ResultOps.pure(value)
  }

  //===========================================================================
  //======================== Result from special cased Result =======================
  //===========================================================================

  def cond[T](test: Boolean, good: => T, bad: => Anomaly): Result[T] =
    if (test) ResultOps.pure(good) else ResultOps.fail(bad)

  def condWith[T](test: Boolean, good: => Result[T], bad: => Anomaly): Result[T] =
    if (test) good else ResultOps.fail(bad)

  def failOnTrue(test: Boolean, bad: => Anomaly): Result[Unit] =
    if (test) ResultOps.fail(bad) else ResultOps.unit

  def failOnFalse(test: Boolean, bad: => Anomaly): Result[Unit] =
    if (!test) ResultOps.fail(bad) else ResultOps.unit

  def flatCond[T](test: Result[Boolean], good: => T, bad: => Anomaly): Result[T] =
    test.flatMap(b => ResultOps.cond(b, good, bad))

  def flatCondWith[T](test: Result[Boolean], good: => Result[T], bad: => Anomaly): Result[T] =
    test.flatMap(b => ResultOps.condWith(b, good, bad))

  def flatFailOnTrue(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
    test.flatMap(b => if (b) ResultOps.fail(bad) else ResultOps.unit)

  def flatFailOnFalse(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
    test.flatMap(b => if (!b) ResultOps.fail(bad) else ResultOps.unit)

  def flattenOption[T](nopt: Result[Option[T]], ifNone: => Anomaly): Result[T] =
    nopt.flatMap(opt => ResultOps.fromOption(opt, ifNone))

  //===========================================================================
  //======================= Result to various effects =========================
  //===========================================================================

  def asOptionUnsafe[T](value: Result[T]): Option[T] = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => Option(value)
  }

  def asListUnsafe[T](value: Result[T]): List[T] = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => List(value)
  }

  def asTry[T](value: Result[T]): Try[T] = value match {
    case Left(value)  => scala.util.Failure(value.asThrowable)
    case Right(value) => scala.util.Success(value)
  }

  def unsafeGet[T](value: Result[T]): T = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => value
  }

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](value: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] =
    value.right.map(good).left.map(bad)

  def morph[T, R](value: Result[T], good: T => R, bad: Anomaly => R): Result[R] = value match {
    case Left(value)  => ResultOps.pure(bad(value))
    case Right(value) => ResultOps.pure(good(value))
  }

  def recover[T, R >: T](value: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] = value match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => ResultOps.pure(pf(a))
    case _ => value
  }

  def recoverWith[T, R >: T](value: Result[T], pf: PartialFunction[Anomaly, Result[R]]): Result[R] = value match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => pf(a)
    case _ => value
  }

  private val UnitFunction: Any => Unit = _ => ()

  def discardContent[T](value: Result[T]): Result[Unit] =
    value.map(UnitFunction)
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

  def apply[T](value: T): Result[T] =
    Right[Anomaly, T](value)

  def unapply[A <: Anomaly, C](arg: Right[A, C]): Option[C] =
    Right.unapply(arg)
}

object Incorrect {

  def apply[T](bad: Anomaly): Result[T] =
    Left[Anomaly, T](bad)

  def unapply[A <: Anomaly, C](arg: Left[A, C]): Option[A] =
    Left.unapply(arg)
}
