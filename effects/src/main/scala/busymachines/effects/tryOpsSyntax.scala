package busymachines.effects

import busymachines.core.Anomaly

import scala.util._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait TryEffectsSyntaxImplicits {

  implicit def bmCommonsTryEffectsOpsSyntax[T](io: Try[T]): TryEffectsOpsSyntax[T] =
    new TryEffectsOpsSyntax(io)

  implicit def bmCommonsBooleanAsTryOps(b: Boolean): BooleanAsTryOps =
    new BooleanAsTryOps(b)

  implicit def bmCommonsTryBooleanAsTryOps(iob: Try[Boolean]): TryBooleanAsTryOps =
    new TryBooleanAsTryOps(iob)

  implicit def bmCommonsTryOptionAsTryOps[T](iopt: Try[Option[T]]): TryOptionAsTryOps[T] =
    new TryOptionAsTryOps(iopt)

  implicit def bmCommonsTryResultAsTryOps[T](ior: Try[Result[T]]): TryResultAsTryOps[T] =
    new TryResultAsTryOps(ior)

  implicit def bmCommonsTryCompanionOps(io: Try.type): TryCompanionOps =
    new TryCompanionOps(io)

}

/**
  *
  */
final class TryCompanionOps(val io: Try.type) {
  def pure[T](t: T): Try[T] = TryEffectsUtil.pure(t)

  def fail[T](a: Anomaly): Try[T] = TryEffectsUtil.fail(a)

  def failWeak[T](a: Throwable): Try[T] = TryEffectsUtil.failWeak(a)

  def unit: Try[Unit] = TryEffectsUtil.unit

  def fromResult[T](r: Result[T]) = TryEffectsUtil.fromResult(r)

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] = TryEffectsUtil.fromOption(opt, ifNone)

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Try[T] = TryEffectsUtil.fromOptionWeak(opt, ifNone)

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Try[T] = TryEffectsUtil.cond(test, correct, anomaly)

  def condWith[T](test: Boolean, correct: => Try[T], anomaly: => Anomaly): Try[T] =
    TryEffectsUtil.condWith(test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.failOnTrue(test, anomaly)

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.failOnFalse(test, anomaly)

  def flatCond[T](test: Try[Boolean], correct: => T, anomaly: => Anomaly): Try[T] =
    TryEffectsUtil.flatCond(test, correct, anomaly)

  def flatCondWith[T](test: Try[Boolean], correct: => Try[T], anomaly: => Anomaly): Try[T] =
    TryEffectsUtil.flatCondWith(test, correct, anomaly)

  def flatFailOnTrue(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.flatFailOnTrue(test, anomaly)

  def flatFailOnFalse(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
    TryEffectsUtil.flatFailOnFalse(test, anomaly)

  def discardContent[T](f: Try[T]) = TryEffectsUtil.discardContent(f)

  def optionFlatten[T](fopt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
    TryEffectsUtil.flattenOption(fopt, ifNone)

  def optionFlattenWeak[T](fopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
    TryEffectsUtil.flattenOptionWeak(fopt, ifNone)

  def resultFlatten[T](fr: Try[Result[T]]): Try[T] =
    TryEffectsUtil.flattenResult(fr)

  def asResult[T](io: Try[T]): Result[T] = TryEffectsUtil.asResult(io)

  def asIO[T](io: Try[T]): IO[T] = TryEffectsUtil.asIO(io)

  def asFuture[T](io: Try[T]): Future[T] = TryEffectsUtil.asFuture(io)

  def asTask[T](io: Try[T]): Task[T] = TryEffectsUtil.asTask(io)

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
    TryEffectsUtil.bimap(io, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](io: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] =
    TryEffectsUtil.bimapWeak(io, good, bad)

  def morph[T, R](io: Try[T], good: T => R, bad: Throwable => R): Try[R] =
    TryEffectsUtil.morph(io, good, bad)
}

/**
  *
  */
final class TryEffectsOpsSyntax[T](private[this] val tr: Try[T]) {

  def asResult: Result[T] = TryEffectsUtil.asResult(tr)

  def asFuture: Future[T] = TryEffectsUtil.asFuture(tr)

  def asIO: IO[T] = TryEffectsUtil.asIO(tr)

  def asTask: Task[T] = TryEffectsUtil.asTask(tr)

  def discardContent: Try[Unit] = TryEffectsUtil.discardContent(tr)

  def bimap[R](good: T => R, bad: Throwable => Anomaly): Try[R] = TryEffectsUtil.bimap(tr, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[R](good: T => R, bad: Throwable => Throwable): Try[R] = TryEffectsUtil.bimapWeak(tr, good, bad)

  def morph[R](good: T => R, bad: Throwable => R): Try[R] = TryEffectsUtil.morph(tr, good, bad)
}

/**
  *
  *
  */
final class TryOptionAsTryOps[T](private[this] val ropt: Try[Option[T]]) {
  def flatten(ifNone: => Anomaly): Try[T] = TryEffectsUtil.flattenOption(ropt, ifNone)

  def flattenWeak(ifNone: => Throwable): Try[T] = TryEffectsUtil.flattenOptionWeak(ropt, ifNone)
}

final class TryResultAsTryOps[T](private[this] val ior: Try[Result[T]]) {
  def flattenResult: Try[T] = TryEffectsUtil.flattenResult(ior)
}

/**
  *
  *
  */
final class BooleanAsTryOps(private[this] val b: Boolean) {

  def condTry[T](correct: => T, anomaly: => Anomaly): Try[T] = TryEffectsUtil.cond(b, correct, anomaly)

  def condWithTry[T](correct: => Try[T], anomaly: => Anomaly): Try[T] = TryEffectsUtil.condWith(b, correct, anomaly)

  def failOnTrueTry(anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.failOnTrue(b, anomaly)

  def failOnFalseTry(anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.failOnFalse(b, anomaly)

}

/**
  *
  *
  */
final class TryBooleanAsTryOps(private[this] val iob: Try[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): Try[T] = TryEffectsUtil.flatCond(iob, correct, anomaly)

  def condWith[T](correct: => Try[T], anomaly: => Anomaly): Try[T] = TryEffectsUtil.flatCondWith(iob, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.flatFailOnTrue(iob, anomaly)

  def failOnFalse(anomaly: => Anomaly): Try[Unit] = TryEffectsUtil.flatFailOnFalse(iob, anomaly)

}

/**
  *
  */
object TryEffectsUtil {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  /**
    * Careful, this does not catch exceptions, use [[Try.apply]] in that case
    */
  def pure[T](t: T): Try[T] = Success(t)

  def fail[T](a: Anomaly): Try[T] = Failure(a.asThrowable)

  def failWeak[T](a: Throwable): Try[T] = Failure(a)

  val unit: Try[Unit] = Success(())

  //===========================================================================
  //==================== Try from various (pseudo)monads ===================
  //===========================================================================

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Try[T] = {
    opt match {
      case None    => TryEffectsUtil.fail(ifNone)
      case Some(v) => TryEffectsUtil.pure(v)
    }
  }

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Try[T] = {
    opt match {
      case None    => TryEffectsUtil.failWeak(ifNone)
      case Some(v) => TryEffectsUtil.pure(v)
    }
  }

  def fromResult[T](r: Result[T]): Try[T] = r match {
    case Correct(value)     => TryEffectsUtil.pure(value)
    case Incorrect(anomaly) => TryEffectsUtil.fail(anomaly)
  }

  //===========================================================================
  //======================== Try from special cased Try =========================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Try[T] =
    if (test) Try(correct) else TryEffectsUtil.fail(anomaly)

  def condWith[T](test: Boolean, correct: => Try[T], anomaly: => Anomaly): Try[T] =
    if (test) correct else TryEffectsUtil.fail(anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Try[Unit] =
    if (test) TryEffectsUtil.fail(anomaly) else TryEffectsUtil.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Try[Unit] =
    if (!test) TryEffectsUtil.fail(anomaly) else TryEffectsUtil.unit

  def flatCond[T](test: Try[Boolean], correct: => T, anomaly: => Anomaly): Try[T] =
    test flatMap (b => TryEffectsUtil.cond(b, correct, anomaly))

  def flatCondWith[T](test: Try[Boolean], correct: => Try[T], anomaly: => Anomaly): Try[T] =
    test flatMap (b => TryEffectsUtil.condWith(b, correct, anomaly))

  def flatFailOnTrue(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
    test flatMap (b => if (b) TryEffectsUtil.fail(anomaly) else TryEffectsUtil.unit)

  def flatFailOnFalse(test: Try[Boolean], anomaly: => Anomaly): Try[Unit] =
    test flatMap (b => if (!b) TryEffectsUtil.fail(anomaly) else TryEffectsUtil.unit)

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](f: Try[T]): Try[Unit] = f.map(UnitFunction)

  def flattenOption[T](fopt: Try[Option[T]], ifNone: => Anomaly): Try[T] =
    fopt flatMap (opt => TryEffectsUtil.fromOption(opt, ifNone))

  def flattenOptionWeak[T](fopt: Try[Option[T]], ifNone: => Throwable): Try[T] =
    fopt flatMap (opt => TryEffectsUtil.fromOptionWeak(opt, ifNone))

  def flattenResult[T](fr: Try[Result[T]]): Try[T] =
    fr.flatMap(r => TryEffectsUtil.fromResult(r))

  //===========================================================================
  //======================= Try to various (pseudo)monads ======================
  //===========================================================================

  def asResult[T](tr: Try[T]): Result[T] = Result.fromTry(tr)

  def asFuture[T](tr: Try[T]): Future[T] = Future.fromTry(tr)

  def asIO[T](tr: Try[T]): IO[T] = IOEffectsUtil.fromTry(tr)

  def asTask[T](tr: Try[T]): Task[T] = Task.fromTry(tr)

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

  def bimap[T, R](tr: Try[T], good: T => R, bad: Throwable => Anomaly): Try[R] = {
    tr match {
      case Failure(t) => TryEffectsUtil.fail(bad(t))
      case Success(v) => TryEffectsUtil.pure(good(v))
    }
  }

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](tr: Try[T], good: T => R, bad: Throwable => Throwable): Try[R] = {
    tr match {
      case Failure(t) => TryEffectsUtil.failWeak(bad(t))
      case Success(v) => TryEffectsUtil.pure(good(v))
    }
  }

  def morph[T, R](tr: Try[T], good: T => R, bad: Throwable => R): Try[R] =
    tr match {
      case Failure(value) => TryEffectsUtil.pure(bad(value))
      case Success(value) => TryEffectsUtil.pure(good(value))
    }
}
