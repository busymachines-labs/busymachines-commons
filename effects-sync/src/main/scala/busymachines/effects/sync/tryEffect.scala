package busymachines.effects.sync

import busymachines.core.Anomaly

import scala.util._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait TryTypeDefinitons {}

object TrySyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcTryCompanionObjectOps(io: Try.type): CompationObjectOps =
      new CompationObjectOps(io)

    implicit def bmcTryReferenceOps[T](io: Try[T]): ReferenceOps[T] =
      new ReferenceOps(io)

    implicit def bmcTryNestedOptionOps[T](iopt: Try[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(iopt)

    implicit def bmcTryNestedResultOps[T](ior: Try[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(ior)

    implicit def bmcTryBooleanOps(b: Boolean): BooleanOps =
      new BooleanOps(b)

    implicit def bmcTryNestedBooleanOps(iob: Try[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(iob)
  }

  /**
    *
    */
  final class CompationObjectOps(val io: Try.type) {
    def pure[T](t: T): Try[T] = TryOps.pure(t)

    def fail[T](a: Anomaly): Try[T] = TryOps.fail(a)

    def failWeak[T](a: Throwable): Try[T] = TryOps.failWeak(a)

    def unit: Try[Unit] = TryOps.unit

    def fromResult[T](r: Result[T]) = TryOps.fromResult(r)

    def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] = TryOps.fromOption(opt, ifNone)

    def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Try[T] = TryOps.fromOptionWeak(opt, ifNone)

    def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Try[T] = TryOps.cond(test, correct, anomaly)

    def condWeak[T](test: Boolean, correct: => T, throwable: => Throwable): Try[T] =
      TryOps.condWeak(test, correct, throwable)

    def condWith[T](test: Boolean, correct: => Try[T], anomaly: => Anomaly): Try[T] =
      TryOps.condWith(test, correct, anomaly)

    def condWithWeak[T](test: Boolean, correct: => Try[T], throwable: => Throwable): Try[T] =
      TryOps.condWithWeak(test, correct, throwable)

    def flatCond[T](test: Try[Boolean], correct: => T, anomaly: => Anomaly): Try[T] =
      TryOps.flatCond(test, correct, anomaly)

    def flatCondWith[T](test: Try[Boolean], correct: => Try[T], anomaly: => Anomaly): Try[T] =
      TryOps.flatCondWith(test, correct, anomaly)

    def failOnTrue(test: Boolean, anomaly: => Anomaly): Try[Unit] = TryOps.failOnTrue(test, anomaly)

    def failOnFalse(test: Boolean, anomaly: => Anomaly): Try[Unit] = TryOps.failOnFalse(test, anomaly)

    def flatFailOnTrue(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] = TryOps.flatFailOnTrue(test, anomaly)

    def flatFailOnFalse(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
      TryOps.flatFailOnFalse(test, anomaly)

    def discardContent[T](f: Try[T]) = TryOps.discardContent(f)

    def flattenOption[T](fopt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
      TryOps.flattenOption(fopt, ifNone)

    def flattenOptionWeak[T](fopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
      TryOps.flattenOptionWeak(fopt, ifNone)

    def flattenResult[T](fr: Try[Result[T]]): Try[T] =
      TryOps.flattenResult(fr)

    def asResult[T](io: Try[T]): Result[T] = TryOps.asResult(io)

    /**
      * !!! USE WITH CARE !!!
      *
      * Using this is highly discouraged!
      *
      * Only for testing
      */
    def unsafeGet[T](tr: Try[T]): T = tr.get

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def bimap[T, R](io: Try[T], good: T => R, bad: Throwable => Anomaly): Try[R] =
      TryOps.bimap(io, good, bad)

    /**
      * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
      * library interop. Ideally, never at all.
      */
    def bimapWeak[T, R](io: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] =
      TryOps.bimapWeak(io, good, bad)

    def morph[T, R](io: Try[T], good: T => R, bad: Throwable => R): Try[R] =
      TryOps.morph(io, good, bad)
  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val tr: Try[T]) {

    def asResult: Result[T] = TryOps.asResult(tr)

    def unsafeGet: T = TryOps.unsafeGet(tr)

    def discardContent: Try[Unit] = TryOps.discardContent(tr)

    def bimap[R](good: T => R, bad: Throwable => Anomaly): Try[R] = TryOps.bimap(tr, good, bad)

    def bimapWeak[R](good: T => R, bad: Throwable => Throwable): Try[R] = TryOps.bimapWeak(tr, good, bad)

    def morph[R](good: T => R, bad: Throwable => R): Try[R] = TryOps.morph(tr, good, bad)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](private[this] val ropt: Try[Option[T]]) {
    def flattenOption(ifNone: => Anomaly): Try[T] = TryOps.flattenOption(ropt, ifNone)

    def flattenOptionWeak(ifNone: => Throwable): Try[T] = TryOps.flattenOptionWeak(ropt, ifNone)
  }

  /**
    *
    */
  final class NestedResultOps[T](private[this] val ior: Try[Result[T]]) {
    def flattenResult: Try[T] = TryOps.flattenResult(ior)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val b: Boolean) {

    def condTry[T](correct: => T, anomaly: => Anomaly): Try[T] = TryOps.cond(b, correct, anomaly)

    def condTryWeak[T](correct: => T, throwable: => Throwable): Try[T] = TryOps.condWeak(b, correct, throwable)

    def condWithTry[T](correct: => Try[T], anomaly: => Anomaly): Try[T] = TryOps.condWith(b, correct, anomaly)

    def condWithWeakTry[T](correct: => Try[T], throwable: => Throwable): Try[T] =
      TryOps.condWithWeak(b, correct, throwable)

    def failOnTrueTry(anomaly: => Anomaly): Try[Unit] = TryOps.failOnTrue(b, anomaly)

    def failOnFalseTry(anomaly: => Anomaly): Try[Unit] = TryOps.failOnFalse(b, anomaly)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val iob: Try[Boolean]) {

    def cond[T](correct: => T, anomaly: => Anomaly): Try[T] = TryOps.flatCond(iob, correct, anomaly)

    def condWith[T](correct: => Try[T], anomaly: => Anomaly): Try[T] = TryOps.flatCondWith(iob, correct, anomaly)

    def failOnTrue(anomaly: => Anomaly): Try[Unit] = TryOps.flatFailOnTrue(iob, anomaly)

    def failOnFalse(anomaly: => Anomaly): Try[Unit] = TryOps.flatFailOnFalse(iob, anomaly)

  }
}

/**
  *
  */
object TryOps {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t: T): Try[T] = Success(t)

  def success[T](t: T): Try[T] = Success(t)

  def fail[T](a: Anomaly): Try[T] = Failure(a.asThrowable)

  def failure[T](a: Anomaly): Try[T] = Failure(a.asThrowable)

  def failWeak[T](a: Throwable): Try[T] = Failure(a)

  def failureWeak[T](a: Anomaly): Try[T] = Failure(a.asThrowable)

  val unit: Try[Unit] = Success(())

  // —— apply delegates to Try.apply directly in syntax object

  //===========================================================================
  //==================== Try from various other effects =======================
  //===========================================================================

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] = {
    opt match {
      case None    => TryOps.fail(ifNone)
      case Some(v) => TryOps.pure(v)
    }
  }

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Try[T] = {
    opt match {
      case None    => TryOps.failWeak(ifNone)
      case Some(v) => TryOps.pure(v)
    }
  }

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Try[R] =
    elr.toTry(ev)

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Try[R] = {
    elr match {
      case Left(left)   => TryOps.fail(transformLeft(left))
      case Right(value) => TryOps.pure(value)
    }
  }

  def fromEitherWeak[L, R](elr: Either[L, R], transformLeft: L => Throwable): Try[R] = {
    elr match {
      case Left(left)   => TryOps.failWeak(transformLeft(left))
      case Right(value) => TryOps.pure(value)
    }
  }

  def fromResult[T](r: Result[T]): Try[T] = r match {
    case Correct(value)     => TryOps.pure(value)
    case Incorrect(anomaly) => TryOps.fail(anomaly)
  }

  //===========================================================================
  //======================== Try from special cased Try =======================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Try[T] =
    if (test) Try(correct) else TryOps.fail(anomaly)

  def condWeak[T](test: Boolean, correct: => T, anomaly: => Throwable): Try[T] =
    if (test) Try(correct) else TryOps.failWeak(anomaly)

  def condWith[T](test: Boolean, correct: => Try[T], anomaly: => Anomaly): Try[T] =
    if (test) correct else TryOps.fail(anomaly)

  def condWithWeak[T](test: Boolean, correct: => Try[T], throwable: => Throwable): Try[T] =
    if (test) correct else TryOps.failWeak(throwable)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Try[Unit] =
    if (test) TryOps.fail(anomaly) else TryOps.unit

  def failOnTrueWeak(test: Boolean, throwable: => Throwable): Try[Unit] =
    if (test) TryOps.failWeak(throwable) else TryOps.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Try[Unit] =
    if (!test) TryOps.fail(anomaly) else TryOps.unit

  def failOnFalseWeak(test: Boolean, throwable: => Throwable): Try[Unit] =
    if (!test) TryOps.failWeak(throwable) else TryOps.unit

  def flatCond[T](test: Try[Boolean], correct: => T, anomaly: => Anomaly): Try[T] =
    test flatMap (b => TryOps.cond(b, correct, anomaly))

  def flatCondWeak[T](test: Try[Boolean], correct: => T, throwable: => Throwable): Try[T] =
    test flatMap (b => TryOps.condWeak(b, correct, throwable))

  def flatCondWith[T](test: Try[Boolean], correct: => Try[T], anomaly: => Anomaly): Try[T] =
    test flatMap (b => TryOps.condWith(b, correct, anomaly))

  def flatCondWithWeak[T](test: Try[Boolean], correct: => Try[T], throwable: => Throwable): Try[T] =
    test flatMap (b => TryOps.condWithWeak(b, correct, throwable))

  def flatFailOnTrue(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
    test flatMap (b => if (b) TryOps.fail(anomaly) else TryOps.unit)

  def flatFailOnTrueWeak(test: Try[Boolean], throwable: => Throwable): Try[Unit] =
    test flatMap (b => if (b) TryOps.failWeak(throwable) else TryOps.unit)

  def flatFailOnFalse(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
    test flatMap (b => if (!b) TryOps.fail(anomaly) else TryOps.unit)

  def flatFailOnFalseWeak(test: Try[Boolean], throwable: => Throwable): Try[Unit] =
    test flatMap (b => if (!b) TryOps.failWeak(throwable) else TryOps.unit)

  def flattenOption[T](topt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
    topt flatMap (opt => TryOps.fromOption(opt, ifNone))

  def flattenOptionWeak[T](fopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
    fopt flatMap (opt => TryOps.fromOptionWeak(opt, ifNone))

  def flattenResult[T](fr: Try[Result[T]]): Try[T] =
    fr.flatMap(r => TryOps.fromResult(r))

  //===========================================================================
  //======================= Try to various (pseudo)monads ======================
  //===========================================================================

  def unsafeAsOption[T](r: Try[T]): Option[T] = r match {
    case Failure(exception) => throw exception
    case Success(value)     => Option(value)
  }

  def unsafeAsList[T](r: Try[T]): List[T] = r match {
    case Failure(exception) => throw exception
    case Success(value)     => List(value)
  }

  def asResult[T](tr: Try[T]): Result[T] = ResultOps.fromTry(tr)

  def unsafeGet[T](tr: Try[T]): T = tr.get

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](tr: Try[T], good: T => R, bad: Throwable => Anomaly): Try[R] = {
    tr match {
      case Failure(t) => TryOps.fail(bad(t))
      case Success(v) => TryOps.pure(good(v))
    }
  }

  def bimapWeak[T, R](tr: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] = {
    tr match {
      case Failure(t) => TryOps.failWeak(bad(t))
      case Success(v) => TryOps.pure(good(v))
    }
  }

  def morph[T, R](tr: Try[T], good: T => R, bad: Throwable => R): Try[R] =
    tr match {
      case Failure(value) => TryOps.pure(bad(value))
      case Success(value) => TryOps.pure(good(value))
    }

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](f: Try[T]): Try[Unit] = f.map(UnitFunction)
}
