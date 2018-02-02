package busymachines.effects.sync

import busymachines.core.Anomaly

import scala.util.{Failure, Success}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait TryTypeDefinitons {
  type Try[T] = scala.util.Try[T]

  val Try: scala.util.Try.type = scala.util.Try
}

object TrySyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcTryCompanionObjectOps(obj: Try.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcTryReferenceOps[T](value: Try[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcTryNestedOptionOps[T](nopt: Try[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit def bmcTryNestedResultOps[T](result: Try[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit def bmcTryBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcTryNestedBooleanOps(test: Try[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Try.type) {

    def pure[T](value: T): Try[T] =
      TryOps.pure(value)

    def success[T](value: T): Try[T] =
      TryOps.success(value)

    def fail[T](bad: Anomaly): Try[T] =
      TryOps.fail(bad)

    def failThr[T](bad: Throwable): Try[T] =
      TryOps.failThr(bad)

    def failure[T](bad: Anomaly): Try[T] =
      TryOps.failure(bad)

    def failureThr[T](bad: Throwable): Try[T] =
      TryOps.failureThr(bad)

    def unit: Try[Unit] = TryOps.unit

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] =
      TryOps.fromOption(opt, ifNone)

    def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Try[T] =
      TryOps.fromOptionThr(opt, ifNone)

    def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Try[R] =
      TryOps.fromEither(either, transformLeft)

    def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Try[R] =
      TryOps.fromEitherThr(either)(ev)

    def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Try[R] =
      TryOps.fromEitherThr(either, transformLeft)

    def fromResult[T](result: Result[T]) =
      TryOps.fromResult(result)

    def cond[T](test: Boolean, good: => T, bad: => Anomaly): Try[T] =
      TryOps.cond(test, good, bad)

    def condThr[T](test: Boolean, good: => T, bad: => Throwable): Try[T] =
      TryOps.condThr(test, good, bad)

    def condWith[T](test: Boolean, good: => Try[T], bad: => Anomaly): Try[T] =
      TryOps.condWith(test, good, bad)

    def condWithThr[T](test: Boolean, good: => Try[T], bad: => Throwable): Try[T] =
      TryOps.condWithThr(test, good, bad)

    def flatCond[T](test: Try[Boolean], good: => T, bad: => Anomaly): Try[T] =
      TryOps.flatCond(test, good, bad)

    def flatCondThr[T](test: Try[Boolean], good: => T, bad: => Throwable): Try[T] =
      TryOps.flatCondThr(test, good, bad)

    def flatCondWith[T](test: Try[Boolean], good: => Try[T], bad: => Anomaly): Try[T] =
      TryOps.flatCondWith(test, good, bad)

    def flatCondWithThr[T](test: Try[Boolean], good: => Try[T], bad: => Throwable): Try[T] =
      TryOps.flatCondWithThr(test, good, bad)

    def failOnTrue(test: Boolean, bad: => Anomaly): Try[Unit] =
      TryOps.failOnTrue(test, bad)

    def failOnTrueThr(test: Boolean, bad: => Throwable): Try[Unit] =
      TryOps.failOnTrueThr(test, bad)

    def failOnFalse(test: Boolean, bad: => Anomaly): Try[Unit] =
      TryOps.failOnFalse(test, bad)

    def failOnFalseThr(test: Boolean, bad: => Throwable): Try[Unit] =
      TryOps.failOnFalseThr(test, bad)

    def flatFailOnTrue(test: Try[Boolean], bad: => Anomaly): Try[Unit] =
      TryOps.flatFailOnTrue(test, bad)

    def flatFailOnTrueThr(test: Try[Boolean], bad: => Throwable): Try[Unit] =
      TryOps.flatFailOnTrueThr(test, bad)

    def flatFailOnFalse(test: Try[Boolean], bad: => Anomaly): Try[Unit] =
      TryOps.flatFailOnFalse(test, bad)

    def flatFailOnFalseThr(test: Try[Boolean], bad: => Throwable): Try[Unit] =
      TryOps.flatFailOnFalseThr(test, bad)

    def unpackOption[T](nopt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
      TryOps.unpackOption(nopt, ifNone)

    def unpackOptionThr[T](nopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
      TryOps.unpackOptionThr(nopt, ifNone)

    def flattenResult[T](value: Try[Result[T]]): Try[T] =
      TryOps.flattenResult(value)

    def asOptionUnsafe[T](value: Try[T]): Option[T] =
      TryOps.asOptionUnsafe(value)

    def asListUnsafe[T](value: Try[T]): List[T] =
      TryOps.asListUnsafe(value)

    def asEither[T](value: Try[T]): Either[Throwable, T] =
      value.toEither

    def asResult[T](value: Try[T]): Result[T] =
      TryOps.asResult(value)

    def unsafeGet[T](value: Try[T]): T =
      value.get

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def bimap[T, R](value: Try[T], good: T => R, bad: Throwable => Anomaly): Try[R] =
      TryOps.bimap(value, good, bad)

    def bimapThr[T, R](value: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] =
      TryOps.bimapThr(value, good, bad)

    def morph[T, R](value: Try[T], good: T => R, bad: Throwable => R): Try[R] =
      TryOps.morph(value, good, bad)

    def discardContent[_](value: Try[_]): Try[Unit] =
      TryOps.discardContent(value)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Try[T]) {

    def asOptionUnsafe(): Option[T] =
      TryOps.asOptionUnsafe(value)

    def asListUnsafe(): List[T] =
      TryOps.asListUnsafe(value)

    def asEither: Either[Throwable, T] =
      value.toEither

    def asResult: Result[T] =
      TryOps.asResult(value)

    def unsafeGet(): T =
      TryOps.unsafeGet(value)

    def bimap[R](good: T => R, bad: Throwable => Anomaly): Try[R] =
      TryOps.bimap(value, good, bad)

    def bimapThr[R](good: T => R, bad: Throwable => Throwable): Try[R] =
      TryOps.bimapThr(value, good, bad)

    def morph[R](good: T => R, bad: Throwable => R): Try[R] =
      TryOps.morph(value, good, bad)

    def discardContent: Try[Unit] =
      TryOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val nopt: Try[Option[T]]) {
    def unpack(ifNone: => Anomaly): Try[T] = TryOps.unpackOption(nopt, ifNone)

    def unpackThr(ifNone: => Throwable): Try[T] = TryOps.unpackOptionThr(nopt, ifNone)
  }

  /**
    *
    */
  final class NestedResultOps[T](private[this] val result: Try[Result[T]]) {
    def flattenResult: Try[T] = TryOps.flattenResult(result)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condTry[T](good: => T, bad: => Anomaly): Try[T] =
      TryOps.cond(test, good, bad)

    def condTryThr[T](good: => T, bad: => Throwable): Try[T] =
      TryOps.condThr(test, good, bad)

    def condWithTry[T](good: => Try[T], bad: => Anomaly): Try[T] =
      TryOps.condWith(test, good, bad)

    def condWithTryThr[T](good: => Try[T], bad: => Throwable): Try[T] =
      TryOps.condWithThr(test, good, bad)

    def failOnTrueTry(bad: => Anomaly): Try[Unit] =
      TryOps.failOnTrue(test, bad)

    def failOnTrueTryThr(bad: => Throwable): Try[Unit] =
      TryOps.failOnTrueThr(test, bad)

    def failOnFalseTry(bad: => Anomaly): Try[Unit] =
      TryOps.failOnFalse(test, bad)

    def failOnFalseTryThr(bad: => Throwable): Try[Unit] =
      TryOps.failOnFalseThr(test, bad)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Try[Boolean]) {

    def cond[T](good: => T, bad: => Anomaly): Try[T] =
      TryOps.flatCond(test, good, bad)

    def condThr[T](good: => T, bad: => Throwable): Try[T] =
      TryOps.flatCondThr(test, good, bad)

    def condWith[T](good: => Try[T], bad: => Anomaly): Try[T] =
      TryOps.flatCondWith(test, good, bad)

    def condWithThr[T](good: => Try[T], bad: => Throwable): Try[T] =
      TryOps.flatCondWithThr(test, good, bad)

    def failOnTrue(bad: => Anomaly): Try[Unit] =
      TryOps.flatFailOnTrue(test, bad)

    def failOnTrueThr(bad: => Throwable): Try[Unit] =
      TryOps.flatFailOnTrueThr(test, bad)

    def failOnFalse(bad: => Anomaly): Try[Unit] =
      TryOps.flatFailOnFalse(test, bad)

    def failOnFalseThr(bad: => Throwable): Try[Unit] =
      TryOps.flatFailOnFalseThr(test, bad)

  }
}

/**
  *
  */
object TryOps {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t: T): Try[T] =
    Success(t)

  def success[T](t: T): Try[T] =
    Success(t)

  def fail[T](bad: Anomaly): Try[T] =
    Failure(bad.asThrowable)

  def failure[T](bad: Anomaly): Try[T] =
    Failure(bad.asThrowable)

  def failThr[T](thr: Throwable): Try[T] =
    Failure(thr)

  def failureThr[T](thr: Throwable): Try[T] =
    Failure(thr)

  val unit: Try[Unit] =
    Success(())

  // —— apply delegates to Try.apply directly in syntax object

  //===========================================================================
  //==================== Try from various other effects =======================
  //===========================================================================

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] = opt match {
    case None        => TryOps.fail(ifNone)
    case Some(value) => TryOps.pure(value)
  }

  def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Try[T] = opt match {
    case None        => TryOps.failThr(ifNone)
    case Some(value) => TryOps.pure(value)
  }

  def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Try[R] =
    either.toTry(ev)

  def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Try[R] = either match {
    case Left(left)   => TryOps.fail(transformLeft(left))
    case Right(value) => TryOps.pure(value)
  }

  def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Try[R] = either match {
    case Left(left)   => TryOps.failThr(transformLeft(left))
    case Right(value) => TryOps.pure(value)
  }

  def fromResult[T](r: Result[T]): Try[T] = r match {
    case Correct(value)     => TryOps.pure(value)
    case Incorrect(anomaly) => TryOps.fail(anomaly)
  }

  //===========================================================================
  //======================== Try from special cased Try =======================
  //===========================================================================

  def cond[T](test: Boolean, good: => T, bad: => Anomaly): Try[T] =
    if (test) TryOps.pure(good) else TryOps.fail(bad)

  def condThr[T](test: Boolean, good: => T, bad: => Throwable): Try[T] =
    if (test) TryOps.pure(good) else TryOps.failThr(bad)

  def condWith[T](test: Boolean, good: => Try[T], bad: => Anomaly): Try[T] =
    if (test) good else TryOps.fail(bad)

  def condWithThr[T](test: Boolean, good: => Try[T], bad: => Throwable): Try[T] =
    if (test) good else TryOps.failThr(bad)

  def failOnTrue(test: Boolean, bad: => Anomaly): Try[Unit] =
    if (test) TryOps.fail(bad) else TryOps.unit

  def failOnTrueThr(test: Boolean, bad: => Throwable): Try[Unit] =
    if (test) TryOps.failThr(bad) else TryOps.unit

  def failOnFalse(test: Boolean, bad: => Anomaly): Try[Unit] =
    if (!test) TryOps.fail(bad) else TryOps.unit

  def failOnFalseThr(test: Boolean, bad: => Throwable): Try[Unit] =
    if (!test) TryOps.failThr(bad) else TryOps.unit

  def flatCond[T](test: Try[Boolean], good: => T, bad: => Anomaly): Try[T] =
    test.flatMap(b => TryOps.cond(b, good, bad))

  def flatCondThr[T](test: Try[Boolean], good: => T, bad: => Throwable): Try[T] =
    test.flatMap(b => TryOps.condThr(b, good, bad))

  def flatCondWith[T](test: Try[Boolean], good: => Try[T], bad: => Anomaly): Try[T] =
    test.flatMap(b => TryOps.condWith(b, good, bad))

  def flatCondWithThr[T](test: Try[Boolean], good: => Try[T], bad: => Throwable): Try[T] =
    test.flatMap(b => TryOps.condWithThr(b, good, bad))

  def flatFailOnTrue(test: Try[Boolean], bad: => Anomaly): Try[Unit] =
    test.flatMap(b => if (b) TryOps.fail(bad) else TryOps.unit)

  def flatFailOnTrueThr(test: Try[Boolean], bad: => Throwable): Try[Unit] =
    test.flatMap(b => if (b) TryOps.failThr(bad) else TryOps.unit)

  def flatFailOnFalse(test: Try[Boolean], bad: => Anomaly): Try[Unit] =
    test.flatMap(b => if (!b) TryOps.fail(bad) else TryOps.unit)

  def flatFailOnFalseThr(test: Try[Boolean], bad: => Throwable): Try[Unit] =
    test.flatMap(b => if (!b) TryOps.failThr(bad) else TryOps.unit)

  def unpackOption[T](nopt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
    nopt.flatMap(opt => TryOps.fromOption(opt, ifNone))

  def unpackOptionThr[T](nopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
    nopt.flatMap(opt => TryOps.fromOptionThr(opt, ifNone))

  def flattenResult[T](result: Try[Result[T]]): Try[T] =
    result.flatMap(r => TryOps.fromResult(r))

  //===========================================================================
  //========================== Try to various effects =========================
  //===========================================================================

  def asOptionUnsafe[T](value: Try[T]): Option[T] = value match {
    case Failure(exception) => throw exception
    case Success(value)     => Option(value)
  }

  def asListUnsafe[T](value: Try[T]): List[T] = value match {
    case Failure(exception) => throw exception
    case Success(value)     => List(value)
  }

  // —— asEither —— is aliased to Try#toEither directly in syntax classes

  def asResult[T](value: Try[T]): Result[T] = Result.fromTry(value)

  def unsafeGet[T](value: Try[T]): T = value.get

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](value: Try[T], good: T => R, bad: Throwable => Anomaly): Try[R] = value match {
    case Failure(t) => TryOps.fail(bad(t))
    case Success(v) => TryOps.pure(good(v))
  }

  def bimapThr[T, R](value: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] = value match {
    case Failure(t) => TryOps.failThr(bad(t))
    case Success(v) => TryOps.pure(good(v))
  }

  def morph[T, R](value: Try[T], good: T => R, bad: Throwable => R): Try[R] = value match {
    case Failure(value) => TryOps.pure(bad(value))
    case Success(value) => TryOps.pure(good(value))
  }

  private val UnitFunction: Any => Unit = _ => ()

  def discardContent[T](value: Try[T]): Try[Unit] =
    value.map(UnitFunction)
}
