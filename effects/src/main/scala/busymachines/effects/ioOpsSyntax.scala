package busymachines.effects

import busymachines.core.Anomaly
import busymachines.core.CatastrophicError

import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait IOEffectsSyntaxImplicits {}

/**
  *
  */
final class IOCompanionOps(val io: IO.type) {}

/**
  *
  */
final class IOEffectsOpsSyntax[T](private[this] val io: IO[T]) {}

/**
  *
  *
  */
final class OptionAsIOOps[T](private[this] val opt: Option[T]) {
  def asIO(ifNone: => Anomaly): IO[T] = IOEffectsUtil.fromOption(opt, ifNone)

  def asIOWeak(ifNone: => Throwable): IO[T] = IOEffectsUtil.fromOptionWeak(opt, ifNone)

}

/**
  *
  *
  */
final class IOOptionAsIOOps[T](private[this] val ropt: IO[Option[T]]) {
  def flatten(ifNone: => Anomaly): IO[T] = IOEffectsUtil.optionFlatten(ropt, ifNone)

  def flattenWeak(ifNone: => Throwable): IO[T] = IOEffectsUtil.optionFlattenWeak(ropt, ifNone)
}

/**
  *
  *
  */
final class EitherAsIOOps[L, R](private[this] val eit: Either[L, R]) {

  def asIO(implicit ev: L <:< Throwable): IO[R] = IOEffectsUtil.fromEither(eit)

  def asIO(transformLeft: L => Anomaly): IO[R] = IOEffectsUtil.fromEither(eit, transformLeft)
}

/**
  *
  *
  */
final class TryAsIOOps[T](private[this] val t: Try[T]) {

  def asIO: IO[T] = IOEffectsUtil.fromTry(t)
}

/**
  *
  *
  */
final class BooleanAsIOOps(private[this] val b: Boolean) {

  def cond[T](correct: => T, anomaly: => Anomaly): IO[T] = IOEffectsUtil.cond(b, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnTrue(b, anomaly)

  def failOnFalse(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnFalse(b, anomaly)

  def effectOnTrue[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnTrue(b, eff)

  def effectOnFalse[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnFalse(b, eff)
}

/**
  *
  *
  */
final class IOBooleanAsIOOps(private[this] val iob: IO[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): IO[T] = IOEffectsUtil.flatCond(iob, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.flatFailOnTrue(iob, anomaly)

  def failOnFalse(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.flatFailOnFalse(iob, anomaly)

  def effectOnTrue[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.flatEffectOnTrue(iob, eff)

  def effectOnFalse[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.flatEffectOnFalse(iob, eff)
}

/**
  *
  */
object IOEffectsUtil {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def fail[T](a: Anomaly): IO[T] = IO.raiseError(a.asThrowable)

  //===========================================================================
  //==================== IO from various (pseudo)monads ===================
  //===========================================================================

  def fromEither[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): IO[R] = {
    elr match {
      case Left(left) =>
        ev(left) match {
          case a: Anomaly => IOEffectsUtil.fail(a)
          case NonFatal(t) => IOEffectsUtil.fail(CatastrophicError(t))
        }
      case Right(value) => IO.pure(value)
    }
  }

  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): IO[R] = {
    elr match {
      case Left(left)   => IOEffectsUtil.fail(transformLeft(left))
      case Right(value) => IO.pure(value)
    }
  }

  def fromResult[T](r: Result[T]): IO[T] = r match {
    case Correct(value)     => IO.pure(value)
    case Incorrect(anomaly) => IO.raiseError(anomaly.asThrowable)
  }

  def fromTry[T](t: Try[T]): IO[T] = t match {
    case Failure(a: Anomaly) => IOEffectsUtil.fail(a)
    case Failure(NonFatal(r)) => IOEffectsUtil.fail(CatastrophicError(r))
    case Success(value)       => IO.pure(value)
  }

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] = {
    opt match {
      case None    => IOEffectsUtil.fail(ifNone)
      case Some(v) => IO.pure(v)
    }
  }

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): IO[T] = {
    opt match {
      case None    => IO.raiseError(ifNone)
      case Some(v) => IO.pure(v)
    }
  }

  def optionFlatten[T](fopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    fopt flatMap (opt => IOEffectsUtil.fromOption(opt, ifNone))

  def optionFlattenWeak[T](fopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    fopt flatMap (opt => IOEffectsUtil.fromOptionWeak(opt, ifNone))

  /**
    * !!! Use with caution !!!
    * Use this iff you are certain that the given future is pure.
    *
    * 99% of the time you need [[fromFutureSuspend]]
    *
    */
  def fromFuture[T](f: Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO.pure(f))

  def fromFutureSuspend[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] =
    IO.fromFuture(IO(f))

  /**
    * Alias for [[Task#toIO]]
    */
  def fromTask[T](t: Task[T])(implicit scheduler: Scheduler): IO[T] = t.toIO

  //===========================================================================
  //======================== IO from special cased IO =========================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): IO[T] =
    if (test) IO(correct) else IOEffectsUtil.fail(anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): IO[Unit] =
    if (test) IOEffectsUtil.fail(anomaly) else IO.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): IO[Unit] =
    if (!test) IOEffectsUtil.fail(anomaly) else IO.unit

  def flatCond[T](test: IO[Boolean], correct: => T, anomaly: => Anomaly): IO[T] =
    test flatMap (b => IOEffectsUtil.cond(b, correct, anomaly))

  def flatFailOnTrue(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] =
    test flatMap (b => if (b) IOEffectsUtil.fail(anomaly) else IO.unit)

  def flatFailOnFalse(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] =
    test flatMap (b => if (!b) IOEffectsUtil.fail(anomaly) else IO.unit)

  def effectOnTrue[T](test: Boolean, eff: => IO[T]): IO[Unit] =
    if (test) IOEffectsUtil.discard(eff) else IO.unit

  def effectOnFalse[T](test: Boolean, eff: => IO[T]): IO[Unit] =
    if (!test) IOEffectsUtil.discard(eff) else IO.unit

  def flatEffectOnTrue[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] =
    test flatMap (b => if (b) IOEffectsUtil.discard(eff) else IO.unit)

  def flatEffectOnFalse[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] =
    test flatMap (b => if (!b) IOEffectsUtil.discard(eff) else IO.unit)

  private val UnitFunction: Any => Unit = _ => ()
  def discard[T](f: IO[T]): IO[Unit] = f.map(UnitFunction)

  //===========================================================================
  //======================= IO to various (pseudo)monads ======================
  //===========================================================================

  def asTask[T](r: IO[T]): Task[T] =
    Task.fromIO(r)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture[T](r: IO[T]): Future[T] = r.unsafeToFuture()

  /**
    * Similar to [[IO#attempt]], but gives you a result instead of an Either
    */
  def asResult[T](r: IO[T]): IO[Result[T]] = {
    r.attempt.map(e => Result.fromEither(e))
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeResult[T](r: IO[T]): Result[T] = Result(r.unsafeRunSync())

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet[T](r: IO[T]): T = r.unsafeRunSync()

  def syncUnsafeAsResult[T](r: IO[T]): Result[T] =
    IOEffectsUtil.asResult(r).unsafeRunSync()

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](r: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] = {
    r.attempt.flatMap {
      case Left(t)  => IOEffectsUtil.fail(bad(t))
      case Right(v) => IO.pure(good(v))
    }
  }

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](r: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] = {
    r.attempt.flatMap {
      case Left(t)  => IO.raiseError(bad(t))
      case Right(v) => IO.pure(good(v))
    }
  }

  def morph[T, R](io: IO[T], good: T => R, bad: Throwable => R): IO[R] =
    io.attempt.flatMap {
      case Left(value)  => IO.pure(bad(value))
      case Right(value) => IO.pure(good(value))
    }
}
