package busymachines.effects.result

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
trait ResultSyntaxImplicits {

  implicit def bmCommonsResultCompanionOps(t: Result.type): ResultEffectSyncSyntax.ResultCompanionOps =
    new ResultEffectSyncSyntax.ResultCompanionOps(t)

  implicit def bmCommonsResultOps[T](t: Result[T]): ResultEffectSyncSyntax.ResultOps[T] =
    new ResultEffectSyncSyntax.ResultOps(t)

  implicit def bmCommonsOptionAsResultOps[T](opt: Option[T]): ResultEffectSyncSyntax.OptionAsResultOps[T] =
    new ResultEffectSyncSyntax.OptionAsResultOps(opt)

  implicit def bmCommonsResultOptionAsResultOps[T](
    ropt: Result[Option[T]]
  ): ResultEffectSyncSyntax.ResultOptionAsResultOps[T] =
    new ResultEffectSyncSyntax.ResultOptionAsResultOps(ropt)

  implicit def bmCommonsTryAsResultOps[T](tr: Try[T]): ResultEffectSyncSyntax.TryAsResultOps[T] =
    new ResultEffectSyncSyntax.TryAsResultOps(tr)

  implicit def bmCommonsEitherAsResultOps[L, R](eit: Either[L, R]): ResultEffectSyncSyntax.EitherAsResultOps[L, R] =
    new ResultEffectSyncSyntax.EitherAsResultOps(eit)

  implicit def bmCommonsBooleanAsResultOps(b: Boolean): ResultEffectSyncSyntax.BooleanAsResultOps =
    new ResultEffectSyncSyntax.BooleanAsResultOps(b)
}

/**
  *
  */
object ResultEffectSyncSyntax {

  /**
    *
    */
  final class ResultCompanionOps(val r: Result.type) extends AnyVal {
    //===========================================================================
    //========================== Primary constructors ===========================
    //===========================================================================

    def pure[T](t:    T): Result[T] = ResultOpsUtil.pure(t)
    def correct[T](t: T): Result[T] = ResultOpsUtil.correct(t)

    def fail[T](a:      Anomaly): Result[T] = ResultOpsUtil.fail(a)
    def incorrect[T](a: Anomaly): Result[T] = ResultOpsUtil.incorrect(a)

    def unit: Result[Unit] = ResultOpsUtil.unit

    /**
      * Useful when one wants to do interop with unknown 3rd party code and you cannot
      * trust not to throw exceptions in your face
      */
    def apply[T](thunk: => T): Result[T] =
      ResultOpsUtil.apply(thunk)

    //===========================================================================
    //==================== Result from various (pseudo)monads ===================
    //===========================================================================

    def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] =
      ResultOpsUtil.fromEither(elr)

    def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] =
      ResultOpsUtil.fromEither(elr, transformLeft)

    def fromTry[T](t: Try[T]): Result[T] = ResultOpsUtil.fromTry(t)

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] =
      ResultOpsUtil.fromOption(opt, ifNone)

    //===========================================================================
    //==================== Result from special cased Result =====================
    //===========================================================================

    def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Result[T] =
      ResultOpsUtil.cond(test, correct, anomaly)

    def failOnTrue(test: Boolean, anomaly: => Anomaly): Result[Unit] =
      ResultOpsUtil.failOnTrue(test, anomaly)

    def failOnFalse(test: Boolean, anomaly: => Anomaly): Result[Unit] =
      ResultOpsUtil.failOnFalse(test, anomaly)

    def flatCond[T](test: Result[Boolean], correct: => T, anomaly: => Anomaly): Result[T] =
      ResultOpsUtil.flatCond(test, correct, anomaly)

    def flatFailOnTrue(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
      ResultOpsUtil.flatFailOnTrue(test, anomaly)

    def flatFailOnFalse(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
      ResultOpsUtil.flatFailOnFalse(test, anomaly)

    //===========================================================================
    //===================== Result to various (pseudo)monads ====================
    //===========================================================================

    def unsafeAsOption[T](r: Result[T]): Option[T] =
      ResultOpsUtil.unsafeAsOption(r)

    def unsafeAsList[T](r: Result[T]): List[T] =
      ResultOpsUtil.unsafeAsList(r)

    def asTry[T](r: Result[T]): Try[T] =
      ResultOpsUtil.asTry(r)

    def unsafeGet[T](r: Result[T]): T =
      ResultOpsUtil.unsafeGet(r)

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def bimap[T, R](r: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] =
      ResultOpsUtil.bimap(r, good, bad)

    def morph[T, R](r: Result[T], good: T => R, bad: Anomaly => R): Result[R] =
      ResultOpsUtil.morph(r, good, bad)

    def recover[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] =
      ResultOpsUtil.recover(r, pf)

    def recoverWith[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, Result[R]]): Result[R] =
      ResultOpsUtil.recoverWith(r, pf)

    def discardContent[T](r: Result[T]): Result[Unit] =
      ResultOpsUtil.discardContent(r)
  }

  /**
    *
    */
  final class ResultOps[T](val r: Result[T]) extends AnyVal {

    def bimap[R](good: T => R, bad: Anomaly => Anomaly): Result[R] = ResultOpsUtil.bimap(r, good, bad)

    /**
      * Used to transform the underlying [[Result]] into a [[Correct]] one.
      * The functions should be pure, and not throw any exception.
      * @return
      *   A [[Correct]] [[Result]], where each branch is transformed as specified
      */
    def morph[R](good: T => R, bad: Anomaly => R): Result[R] = ResultOpsUtil.morph(r, good, bad)

    def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] = ResultOpsUtil.recover(r, pf)

    def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] = ResultOpsUtil.recoverWith(r, pf)

    def discardContent: Result[Unit] = ResultOpsUtil.discardContent(r)

    //===========================================================================
    //===================== Result to various (pseudo)monads ====================
    //===========================================================================

    def unsafeAsOption: Option[T] = ResultOpsUtil.unsafeAsOption(r)

    def unsafeAsList: List[T] = ResultOpsUtil.unsafeAsList(r)

    def asTry: Try[T] = ResultOpsUtil.asTry(r)

    def unsafeGet: T = ResultOpsUtil.unsafeGet(r)
  }

  /**
    *
    */
  final class OptionAsResultOps[T](val opt: Option[T]) extends AnyVal {

    def asResult(ifNone: => Anomaly): Result[T] = ResultOpsUtil.fromOption(opt, ifNone)

  }

  /**
    *
    */
  final class ResultOptionAsResultOps[T](val ropt: Result[Option[T]]) extends AnyVal {

    def flatten(ifNone: => Anomaly): Result[T] =
      ropt flatMap (opt => ResultOpsUtil.fromOption(opt, ifNone))
  }

  /**
    *
    */
  final class EitherAsResultOps[L, R](private[this] val eit: Either[L, R]) {

    def asResult(implicit ev: L <:< Throwable): Result[R] = ResultOpsUtil.fromEither(eit)

    def asResult(transformLeft: L => Anomaly): Result[R] = ResultOpsUtil.fromEither(eit, transformLeft)
  }

  /**
    *
    */
  final class TryAsResultOps[T](private[this] val t: Try[T]) {

    def asResult: Result[T] = ResultOpsUtil.fromTry(t)
  }

  /**
    *
    *
    */
  final class BooleanAsResultOps(private[this] val b: Boolean) {

    def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = ResultOpsUtil.cond(b, correct, anomaly)

    def failOnTrue(anomaly: => Anomaly): Result[Unit] = ResultOpsUtil.failOnTrue(b, anomaly)

    def failOnFalse(anomaly: => Anomaly): Result[Unit] = ResultOpsUtil.failOnFalse(b, anomaly)
  }

  /**
    *
    *
    */
  final class ResultBooleanAsResultOps(private[this] val br: Result[Boolean]) {

    def cond[T](correct: => T, anomaly: => Anomaly): Result[T] = ResultOpsUtil.flatCond(br, correct, anomaly)

    def failOnTrue(anomaly: => Anomaly): Result[Unit] = ResultOpsUtil.flatFailOnTrue(br, anomaly)

    def failOnFalse(anomaly: => Anomaly): Result[Unit] = ResultOpsUtil.flatFailOnFalse(br, anomaly)
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

object ResultOpsUtil {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t:    T): Result[T] = Correct(t)
  def correct[T](t: T): Result[T] = Correct(t)

  def fail[T](a:      Anomaly): Result[T] = Incorrect(a)
  def incorrect[T](a: Anomaly): Result[T] = Incorrect(a)

  val unit: Result[Unit] = Correct(())

  /**
    * Useful when one wants to do interop with unknown 3rd party code and you cannot
    * trust not to throw exceptions in your face
    */
  def apply[T](thunk: => T): Result[T] = {
    try {
      ResultOpsUtil.pure(thunk)
    } catch {
      case a: Anomaly                  => ResultOpsUtil.incorrect(a)
      case t: Throwable if NonFatal(t) => ResultOpsUtil.incorrect(CatastrophicError(t))
    }
  }

  //===========================================================================
  //==================== Result from various (pseudo)monads ===================
  //===========================================================================

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => ResultOpsUtil.incorrect(a)
          case NonFatal(t) => ResultOpsUtil.incorrect(CatastrophicError(t))
        }
      case Right(value) => ResultOpsUtil.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = {
    elr match {
      case Left(left)   => ResultOpsUtil.incorrect(transformLeft(left))
      case Right(value) => ResultOpsUtil.pure(value)
    }
  }

  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => ResultOpsUtil.incorrect(a)
    case Failure(NonFatal(r)) => ResultOpsUtil.incorrect(CatastrophicError(r))
    case Success(value)       => ResultOpsUtil.pure(value)
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = {
    opt match {
      case None    => ResultOpsUtil.incorrect(ifNone)
      case Some(v) => ResultOpsUtil.pure(v)
    }
  }

  //===========================================================================
  //==================== Result from special cased Result =====================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Result[T] =
    Either.cond[Anomaly, T](test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (test) ResultOpsUtil.incorrect(anomaly) else ResultOpsUtil.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Result[Unit] =
    if (!test) ResultOpsUtil.incorrect(anomaly) else ResultOpsUtil.unit

  def flatCond[T](test: Result[Boolean], correct: => T, anomaly: => Anomaly): Result[T] =
    test flatMap (b => ResultOpsUtil.cond(b, correct, anomaly))

  def flatFailOnTrue(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (b) ResultOpsUtil.incorrect(anomaly) else ResultOpsUtil.unit)

  def flatFailOnFalse(test: Result[Boolean], anomaly: => Anomaly): Result[Unit] =
    test flatMap (b => if (!b) ResultOpsUtil.incorrect(anomaly) else ResultOpsUtil.unit)

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
    case Left(value)  => ResultOpsUtil.pure(bad(value))
    case Right(value) => ResultOpsUtil.pure(good(value))
  }

  def recover[T, R >: T](r: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] = r match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => ResultOpsUtil.pure(pf(a))
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
