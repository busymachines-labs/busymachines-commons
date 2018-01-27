package busymachines.effects

import busymachines.core.Anomaly

import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait IOEffectsSyntaxImplicits {

  implicit def bmCommonsIOEffectsOpsSyntax[T](io: IO[T]): IOEffectsOpsSyntax[T] =
    new IOEffectsOpsSyntax(io)

  implicit def bmCommonsBooleanAsIOOps(b: Boolean): BooleanAsIOOps =
    new BooleanAsIOOps(b)

  implicit def bmCommonsIOBooleanAsIOOps(iob: IO[Boolean]): IOBooleanAsIOOps =
    new IOBooleanAsIOOps(iob)

  implicit def bmCommonsIOOptionAsIOOps[T](iopt: IO[Option[T]]): IOOptionAsIOOps[T] =
    new IOOptionAsIOOps(iopt)

  implicit def bmCommonsIOCompanionOps(io: IO.type): IOCompanionOps =
    new IOCompanionOps(io)

}

/**
  *
  */
final class IOCompanionOps(val io: IO.type) {
  def fail[T](a: Anomaly): IO[T] = IOEffectsUtil.fail(a)

  def fromResult[T](r: Result[T]) = IOEffectsUtil.fromResult(r)

  def fromResultSuspend[T](r: => Result[T]) = IOEffectsUtil.fromResultSuspend(r)

  def fromTry[T](t: Try[T]): IO[T] = IOEffectsUtil.fromTry(t)

  def fromTrySuspend[T](t: => Try[T]): IO[T] = IOEffectsUtil.fromTrySuspend(t)

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] = IOEffectsUtil.fromOption(opt, ifNone)

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): IO[T] = IOEffectsUtil.fromOptionWeak(opt, ifNone)

  def fromPureFuture[T](f: Future[T])(implicit ec: ExecutionContext): IO[T] = IOEffectsUtil.fromPureFuture(f)

  def fromFutureSuspend[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] = IOEffectsUtil.fromFutureSuspend(f)

  def fromTask[T](t: Task[T])(implicit scheduler: Scheduler): IO[T] = IOEffectsUtil.fromTask(t)

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): IO[T] = IOEffectsUtil.cond(test, correct, anomaly)

  def condWith[T](test: Boolean, correct: => IO[T], anomaly: => Anomaly): IO[T] =
    IOEffectsUtil.condWith(test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnTrue(test, anomaly)

  def failOnFalse(test: Boolean, anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnFalse(test, anomaly)

  def flatCond[T](test: IO[Boolean], correct: => T, anomaly: => Anomaly): IO[T] =
    IOEffectsUtil.flatCond(test, correct, anomaly)

  def flatCondWith[T](test: IO[Boolean], correct: => IO[T], anomaly: => Anomaly): IO[T] =
    IOEffectsUtil.flatCondWith(test, correct, anomaly)

  def flatFailOnTrue(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.flatFailOnTrue(test, anomaly)

  def flatFailOnFalse(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.flatFailOnFalse(test, anomaly)

  def effectOnTrue[T](test: Boolean, eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnTrue(test, eff)

  def effectOnFalse[T](test: Boolean, eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnFalse(test, eff)

  def flatEffectOnTrue[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] = IOEffectsUtil.flatEffectOnTrue(test, eff)

  def flatEffectOnFalse[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] = IOEffectsUtil.flatEffectOnFalse(test, eff)

  def discardContent[T](f: IO[T]) = IOEffectsUtil.discardContent(f)

  def optionFlatten[T](fopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    IOEffectsUtil.optionFlatten(fopt, ifNone)

  def optionFlattenWeak[T](fopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    IOEffectsUtil.optionFlattenWeak(fopt, ifNone)

  def resultFlatten[T](fr: IO[Result[T]]): IO[T] =
    IOEffectsUtil.resultFlatten(fr)

  def asTask[T](io: IO[T]): Task[T] = IOEffectsUtil.asTask(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture[T](io: IO[T]): Future[T] = IOEffectsUtil.asFuture(io)

  /**
    * Similar to [[IO#attempt]], but gives you a result instead of an Either
    */
  def asResult[T](io: IO[T]): IO[Result[T]] = IOEffectsUtil.asResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeAsResult[T](io: IO[T]): Result[T] = IOEffectsUtil.syncUnsafeAsResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet[T](io: IO[T]): T = IOEffectsUtil.syncUnsafeGet(io)

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](io: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
    IOEffectsUtil.bimap(io, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](io: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
    IOEffectsUtil.bimapWeak(io, good, bad)

  def morph[T, R](io: IO[T], good: T => R, bad: Throwable => R): IO[R] =
    IOEffectsUtil.morph(io, good, bad)
}

/**
  *
  */
final class IOEffectsOpsSyntax[T](private[this] val io: IO[T]) {

  def asTask: Task[T] = IOEffectsUtil.asTask(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture: Future[T] = IOEffectsUtil.asFuture(io)

  /**
    * Similar to [[IO#attempt]], but gives you a result instead of an Either
    */
  def asResult: IO[Result[T]] = IOEffectsUtil.asResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeAsResult: Result[T] = IOEffectsUtil.syncUnsafeAsResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet: T = IOEffectsUtil.syncUnsafeGet(io)

  def discardContent: IO[Unit] = IOEffectsUtil.discardContent(io)

  def bimap[R](good: T => R, bad: Throwable => Anomaly): IO[R] = IOEffectsUtil.bimap(io, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[R](good: T => R, bad: Throwable => Throwable): IO[R] = IOEffectsUtil.bimapWeak(io, good, bad)

  def morph[R](good: T => R, bad: Throwable => R): IO[R] = IOEffectsUtil.morph(io, good, bad)
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
final class BooleanAsIOOps(private[this] val b: Boolean) {

  def condIO[T](correct: => T, anomaly: => Anomaly): IO[T] = IOEffectsUtil.cond(b, correct, anomaly)

  def condWithIO[T](correct: => IO[T], anomaly: => Anomaly): IO[T] = IOEffectsUtil.condWith(b, correct, anomaly)

  def failOnTrueIO(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnTrue(b, anomaly)

  def failOnFalseIO(anomaly: => Anomaly): IO[Unit] = IOEffectsUtil.failOnFalse(b, anomaly)

  def effectOnTrueIO[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnTrue(b, eff)

  def effectOnFalseIO[T](eff: => IO[T]): IO[Unit] = IOEffectsUtil.effectOnFalse(b, eff)
}

/**
  *
  *
  */
final class IOBooleanAsIOOps(private[this] val iob: IO[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): IO[T] = IOEffectsUtil.flatCond(iob, correct, anomaly)

  def condWith[T](correct: => IO[T], anomaly: => Anomaly): IO[T] = IOEffectsUtil.flatCondWith(iob, correct, anomaly)

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

  def fromResult[T](r: Result[T]): IO[T] = r match {
    case Correct(value)     => IO.pure(value)
    case Incorrect(anomaly) => IO.raiseError(anomaly.asThrowable)
  }

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
    *   val suspendedSideEffect: IO[Int] = IO.fromResultSuspend {
    *     Result {
    *       println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    *       sideEffect = 42
    *       sideEffect
    *     }
    *   }
    *
    *  //this is not thrown:
    *  if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")
    * }}}
    */
  def fromResultSuspend[T](r: => Result[T]): IO[T] =
    IO(r).flatMap(IOEffectsUtil.fromResult)

  def fromTry[T](t: Try[T]): IO[T] = t match {
    case Success(value)       => IO.pure(value)
    case Failure(NonFatal(r)) => IO.raiseError(r)
  }

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
    *   val suspendedSideEffect: IO[Int] = IO.fromResultSuspend {
    *     Try {
    *       println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    *       sideEffect = 42
    *       sideEffect
    *     }
    *   }
    *
    *  //this is not thrown:
    *  if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")
    * }}}
    */
  def fromTrySuspend[T](t: => Try[T]): IO[T] =
    IO(t).flatMap(IOEffectsUtil.fromTry)

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

  /**
    * !!! Use with caution !!!
    * Use this iff you are certain that the given future is pure.
    *
    * 99% of the time you need [[fromFutureSuspend]]
    *
    */
  def fromPureFuture[T](f: Future[T])(implicit ec: ExecutionContext): IO[T] =
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

  def condWith[T](test: Boolean, correct: => IO[T], anomaly: => Anomaly): IO[T] =
    if (test) correct else IOEffectsUtil.fail(anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): IO[Unit] =
    if (test) IOEffectsUtil.fail(anomaly) else IO.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): IO[Unit] =
    if (!test) IOEffectsUtil.fail(anomaly) else IO.unit

  def flatCond[T](test: IO[Boolean], correct: => T, anomaly: => Anomaly): IO[T] =
    test flatMap (b => IOEffectsUtil.cond(b, correct, anomaly))

  def flatCondWith[T](test: IO[Boolean], correct: => IO[T], anomaly: => Anomaly): IO[T] =
    test flatMap (b => IOEffectsUtil.condWith(b, correct, anomaly))

  def flatFailOnTrue(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] =
    test flatMap (b => if (b) IOEffectsUtil.fail(anomaly) else IO.unit)

  def flatFailOnFalse(test: IO[Boolean], anomaly: => Anomaly): IO[Unit] =
    test flatMap (b => if (!b) IOEffectsUtil.fail(anomaly) else IO.unit)

  def effectOnTrue[T](test: Boolean, eff: => IO[T]): IO[Unit] =
    if (test) IOEffectsUtil.discardContent(eff) else IO.unit

  def effectOnFalse[T](test: Boolean, eff: => IO[T]): IO[Unit] =
    if (!test) IOEffectsUtil.discardContent(eff) else IO.unit

  def flatEffectOnTrue[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] =
    test flatMap (b => if (b) IOEffectsUtil.discardContent(eff) else IO.unit)

  def flatEffectOnFalse[T](test: IO[Boolean], eff: => IO[T]): IO[Unit] =
    test flatMap (b => if (!b) IOEffectsUtil.discardContent(eff) else IO.unit)

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](f: IO[T]): IO[Unit] = f.map(UnitFunction)

  def optionFlatten[T](fopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    fopt flatMap (opt => IOEffectsUtil.fromOption(opt, ifNone))

  def optionFlattenWeak[T](fopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    fopt flatMap (opt => IOEffectsUtil.fromOptionWeak(opt, ifNone))

  def resultFlatten[T](fr: IO[Result[T]]): IO[T] =
    fr.flatMap(r => IOEffectsUtil.fromResult(r))

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
  def syncUnsafeAsResult[T](r: IO[T]): Result[T] = Result(r.unsafeRunSync())

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet[T](r: IO[T]): T = r.unsafeRunSync()

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
