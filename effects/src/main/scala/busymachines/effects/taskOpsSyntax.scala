package busymachines.effects

import busymachines.core.Anomaly
import busymachines.duration, duration.FiniteDuration

import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait TaskEffectsSyntaxImplicits {

  implicit def bmCommonsTaskEffectsOpsSyntax[T](io: Task[T]): TaskEffectsOpsSyntax[T] =
    new TaskEffectsOpsSyntax(io)

  implicit def bmCommonsBooleanAsTaskOps(b: Boolean): BooleanAsTaskOps =
    new BooleanAsTaskOps(b)

  implicit def bmCommonsTaskBooleanAsTaskOps(task: Task[Boolean]): TaskBooleanAsTaskOps =
    new TaskBooleanAsTaskOps(task)

  implicit def bmCommonsTaskOptionAsTaskOps[T](topt: Task[Option[T]]): TaskOptionAsTaskOps[T] =
    new TaskOptionAsTaskOps(topt)

  implicit def bmCommonsTaskCompanionOps(io: Task.type): TaskCompanionOps =
    new TaskCompanionOps(io)

}

/**
  *
  */
final class TaskCompanionOps(val io: Task.type) {
  def fail[T](a: Anomaly): Task[T] = TaskEffectsUtil.fail(a)

  def fromResult[T](r: Result[T]) = TaskEffectsUtil.fromResult(r)

  def fromResultSuspend[T](r: => Result[T]) = TaskEffectsUtil.fromResultSuspend(r)

  def fromTry[T](t: Try[T]): Task[T] = TaskEffectsUtil.fromTry(t)

  def fromTrySuspend[T](t: => Try[T]): Task[T] = TaskEffectsUtil.fromTrySuspend(t)

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] = TaskEffectsUtil.fromOption(opt, ifNone)

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Task[T] = TaskEffectsUtil.fromOptionWeak(opt, ifNone)

  def fromPureFuture[T](f: Future[T]): Task[T] = TaskEffectsUtil.fromPureFuture(f)

  def fromFutureSuspend[T](f: => Future[T]): Task[T] =
    TaskEffectsUtil.fromFutureSuspend(f)

  def fromIO[T](io: IO[T]): Task[T] = TaskEffectsUtil.fromIO(io)

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Task[T] = TaskEffectsUtil.cond(test, correct, anomaly)

  def condWith[T](test: Boolean, correct: => Task[T], anomaly: => Anomaly): Task[T] =
    TaskEffectsUtil.condWith(test, correct, anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.failOnTrue(test, anomaly)

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.failOnFalse(test, anomaly)

  def flatCond[T](test: Task[Boolean], correct: => T, anomaly: => Anomaly): Task[T] =
    TaskEffectsUtil.flatCond(test, correct, anomaly)

  def flatCondWith[T](test: Task[Boolean], correct: => Task[T], anomaly: => Anomaly): Task[T] =
    TaskEffectsUtil.flatCondWith(test, correct, anomaly)

  def flatFailOnTrue(test: Task[Boolean], anomaly: => Anomaly): Task[Unit] =
    TaskEffectsUtil.flatFailOnTrue(test, anomaly)

  def flatFailOnFalse(test: Task[Boolean], anomaly: => Anomaly): Task[Unit] =
    TaskEffectsUtil.flatFailOnFalse(test, anomaly)

  def effectOnTrue[T](test: Boolean, eff: => Task[T]): Task[Unit] = TaskEffectsUtil.effectOnTrue(test, eff)

  def effectOnFalse[T](test: Boolean, eff: => Task[T]): Task[Unit] = TaskEffectsUtil.effectOnFalse(test, eff)

  def flatEffectOnTrue[T](test: Task[Boolean], eff: => Task[T]): Task[Unit] =
    TaskEffectsUtil.flatEffectOnTrue(test, eff)

  def flatEffectOnFalse[T](test: Task[Boolean], eff: => Task[T]): Task[Unit] =
    TaskEffectsUtil.flatEffectOnFalse(test, eff)

  def discardContent[T](f: Task[T]) = TaskEffectsUtil.discardContent(f)

  def optionFlatten[T](fopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
    TaskEffectsUtil.optionFlatten(fopt, ifNone)

  def optionFlattenWeak[T](fopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
    TaskEffectsUtil.optionFlattenWeak(fopt, ifNone)

  def resultFlatten[T](fr: Task[Result[T]]): Task[T] =
    TaskEffectsUtil.resultFlatten(fr)

  def asIO[T](io: Task[T])(implicit s: Scheduler): IO[T] = TaskEffectsUtil.asIO(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture[T](io: Task[T])(implicit s: Scheduler): Future[T] = TaskEffectsUtil.asFuture(io)

  /**
    * Similar to [[Task#attempt]], but gives you a result instead of an Either
    */
  def asResult[T](io: Task[T]): Task[Result[T]] = TaskEffectsUtil.asResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeAsResult[T](io: Task[T])(implicit s: Scheduler): Result[T] = TaskEffectsUtil.syncUnsafeAsResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet[T](io: Task[T], timeout: FiniteDuration = duration.minutes(1))(implicit s: Scheduler): T =
    TaskEffectsUtil.syncUnsafeGet(io)

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](io: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] =
    TaskEffectsUtil.bimap(io, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](io: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] =
    TaskEffectsUtil.bimapWeak(io, good, bad)

  def morph[T, R](io: Task[T], good: T => R, bad: Throwable => R): Task[R] =
    TaskEffectsUtil.morph(io, good, bad)
}

/**
  *
  */
final class TaskEffectsOpsSyntax[T](private[this] val io: Task[T]) {

  def asIO(implicit s: Scheduler): IO[T] = TaskEffectsUtil.asIO(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture(implicit s: Scheduler): Future[T] = TaskEffectsUtil.asFuture(io)

  /**
    * Similar to [[Task#attempt]], but gives you a result instead of an Either
    */
  def asResult: Task[Result[T]] = TaskEffectsUtil.asResult(io)

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeAsResult(timeout: FiniteDuration = duration.minutes(1))(implicit s: Scheduler): Result[T] =
    TaskEffectsUtil.syncUnsafeAsResult(io, timeout)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet(timeout: FiniteDuration = duration.minutes(1))(implicit s: Scheduler): T =
    TaskEffectsUtil.syncUnsafeGet(io, timeout)

  def discardContent: Task[Unit] = TaskEffectsUtil.discardContent(io)

  def bimap[R](good: T => R, bad: Throwable => Anomaly): Task[R] = TaskEffectsUtil.bimap(io, good, bad)

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[R](good: T => R, bad: Throwable => Throwable): Task[R] = TaskEffectsUtil.bimapWeak(io, good, bad)

  def morph[R](good: T => R, bad: Throwable => R): Task[R] = TaskEffectsUtil.morph(io, good, bad)
}

/**
  *
  *
  */
final class TaskOptionAsTaskOps[T](private[this] val ropt: Task[Option[T]]) {
  def flattenOpt(ifNone: => Anomaly): Task[T] = TaskEffectsUtil.optionFlatten(ropt, ifNone)

  def flattenOptWeak(ifNone: => Throwable): Task[T] = TaskEffectsUtil.optionFlattenWeak(ropt, ifNone)
}

/**
  *
  *
  */
final class BooleanAsTaskOps(private[this] val b: Boolean) {

  def condTask[T](correct: => T, anomaly: => Anomaly): Task[T] = TaskEffectsUtil.cond(b, correct, anomaly)

  def condWithTask[T](correct: => Task[T], anomaly: => Anomaly): Task[T] = TaskEffectsUtil.condWith(b, correct, anomaly)

  def failOnTrueTask(anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.failOnTrue(b, anomaly)

  def failOnFalseTask(anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.failOnFalse(b, anomaly)

  def effectOnTrueTask[T](eff: => Task[T]): Task[Unit] = TaskEffectsUtil.effectOnTrue(b, eff)

  def effectOnFalseTask[T](eff: => Task[T]): Task[Unit] = TaskEffectsUtil.effectOnFalse(b, eff)
}

/**
  *
  *
  */
final class TaskBooleanAsTaskOps(private[this] val iob: Task[Boolean]) {

  def cond[T](correct: => T, anomaly: => Anomaly): Task[T] = TaskEffectsUtil.flatCond(iob, correct, anomaly)

  def condWith[T](correct: => Task[T], anomaly: => Anomaly): Task[T] =
    TaskEffectsUtil.flatCondWith(iob, correct, anomaly)

  def failOnTrue(anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.flatFailOnTrue(iob, anomaly)

  def failOnFalse(anomaly: => Anomaly): Task[Unit] = TaskEffectsUtil.flatFailOnFalse(iob, anomaly)

  def effectOnTrue[T](eff: => Task[T]): Task[Unit] = TaskEffectsUtil.flatEffectOnTrue(iob, eff)

  def effectOnFalse[T](eff: => Task[T]): Task[Unit] = TaskEffectsUtil.flatEffectOnFalse(iob, eff)
}

/**
  *
  */
object TaskEffectsUtil {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def fail[T](a: Anomaly): Task[T] = Task.raiseError(a.asThrowable)

  //===========================================================================
  //==================== Task from various (pseudo)monads ===================
  //===========================================================================

  def fromResult[T](r: Result[T]): Task[T] = r match {
    case Correct(value)     => Task.pure(value)
    case Incorrect(anomaly) => Task.raiseError(anomaly.asThrowable)
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
    *   val suspendedSideEffect: Task[Int] = Task.fromResultSuspend {
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
  def fromResultSuspend[T](r: => Result[T]): Task[T] =
    Task(r).flatMap(TaskEffectsUtil.fromResult)

  def fromTry[T](t: Try[T]): Task[T] = t match {
    case Success(value)       => Task.pure(value)
    case Failure(NonFatal(r)) => Task.raiseError(r)
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
    *   val suspendedSideEffect: Task[Int] = Task.fromResultSuspend {
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
  def fromTrySuspend[T](t: => Try[T]): Task[T] =
    Task(t).flatMap(TaskEffectsUtil.fromTry)

  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] = {
    opt match {
      case None    => TaskEffectsUtil.fail(ifNone)
      case Some(v) => Task.pure(v)
    }
  }

  def fromOptionWeak[T](opt: Option[T], ifNone: => Throwable): Task[T] = {
    opt match {
      case None    => Task.raiseError(ifNone)
      case Some(v) => Task.pure(v)
    }
  }

  /**
    * !!! Use with caution !!!
    * Use this iff you are certain that the given future is pure.
    *
    * 99% of the time you need [[fromFutureSuspend]]
    *
    */
  def fromPureFuture[T](f: Future[T]): Task[T] =
    Task.fromFuture(f)

  def fromFutureSuspend[T](f: => Future[T]): Task[T] =
    Task.deferFuture(f)

  /**
    * Alias for [[Task#fromIO]]
    */
  def fromIO[T](io: IO[T]): Task[T] = Task.fromIO(io)

  //===========================================================================
  //======================== Task from special cased Task =========================
  //===========================================================================

  def cond[T](test: Boolean, correct: => T, anomaly: => Anomaly): Task[T] =
    if (test) Task(correct) else TaskEffectsUtil.fail(anomaly)

  def condWith[T](test: Boolean, correct: => Task[T], anomaly: => Anomaly): Task[T] =
    if (test) correct else TaskEffectsUtil.fail(anomaly)

  def failOnTrue(test: Boolean, anomaly: => Anomaly): Task[Unit] =
    if (test) TaskEffectsUtil.fail(anomaly) else Task.unit

  def failOnFalse(test: Boolean, anomaly: => Anomaly): Task[Unit] =
    if (!test) TaskEffectsUtil.fail(anomaly) else Task.unit

  def flatCond[T](test: Task[Boolean], correct: => T, anomaly: => Anomaly): Task[T] =
    test flatMap (b => TaskEffectsUtil.cond(b, correct, anomaly))

  def flatCondWith[T](test: Task[Boolean], correct: => Task[T], anomaly: => Anomaly): Task[T] =
    test flatMap (b => TaskEffectsUtil.condWith(b, correct, anomaly))

  def flatFailOnTrue(test: Task[Boolean], anomaly: => Anomaly): Task[Unit] =
    test flatMap (b => if (b) TaskEffectsUtil.fail(anomaly) else Task.unit)

  def flatFailOnFalse(test: Task[Boolean], anomaly: => Anomaly): Task[Unit] =
    test flatMap (b => if (!b) TaskEffectsUtil.fail(anomaly) else Task.unit)

  def effectOnTrue[T](test: Boolean, eff: => Task[T]): Task[Unit] =
    if (test) TaskEffectsUtil.discardContent(eff) else Task.unit

  def effectOnFalse[T](test: Boolean, eff: => Task[T]): Task[Unit] =
    if (!test) TaskEffectsUtil.discardContent(eff) else Task.unit

  def flatEffectOnTrue[T](test: Task[Boolean], eff: => Task[T]): Task[Unit] =
    test flatMap (b => if (b) TaskEffectsUtil.discardContent(eff) else Task.unit)

  def flatEffectOnFalse[T](test: Task[Boolean], eff: => Task[T]): Task[Unit] =
    test flatMap (b => if (!b) TaskEffectsUtil.discardContent(eff) else Task.unit)

  private val UnitFunction: Any => Unit = _ => ()
  def discardContent[T](f: Task[T]): Task[Unit] = f.map(UnitFunction)

  def optionFlatten[T](fopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
    fopt flatMap (opt => TaskEffectsUtil.fromOption(opt, ifNone))

  def optionFlattenWeak[T](fopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
    fopt flatMap (opt => TaskEffectsUtil.fromOptionWeak(opt, ifNone))

  def resultFlatten[T](fr: Task[Result[T]]): Task[T] =
    fr.flatMap(r => TaskEffectsUtil.fromResult(r))

  //===========================================================================
  //======================= Task to various (pseudo)monads ======================
  //===========================================================================

  def asIO[T](r: Task[T])(implicit s: Scheduler): IO[T] = r.toIO

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing and legacy code interop
    */
  def asFuture[T](r: Task[T])(implicit s: Scheduler): Future[T] = r.runAsync

  /**
    * Similar to [[Task#attempt]], but gives you a result instead of an Either
    */
  def asResult[T](r: Task[T]): Task[Result[T]] = {
    r.attempt.map(e => Result.fromEither(e))
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Only for testing
    */
  def syncUnsafeAsResult[T](
    r:          Task[T],
    timeout:    FiniteDuration = duration.minutes(1)
  )(implicit s: Scheduler): Result[T] =
    r.runAsync.syncAsResult(timeout)

  /**
    * !!! USE WITH CARE !!!
    *
    * Using this is highly discouraged!
    *
    * Only for testing
    */
  def syncUnsafeGet[T](
    r:          Task[T],
    timeout:    FiniteDuration = duration.minutes(1)
  )(implicit s: Scheduler): T = r.runAsync.syncUnsafeGet(timeout)

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def bimap[T, R](r: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] = {
    r.attempt.flatMap {
      case Left(t)  => TaskEffectsUtil.fail(bad(t))
      case Right(v) => Task.pure(good(v))
    }
  }

  /**
    * A more generic version of [[bimap]]. Use only for legacy code, or 3rd party
    * library interop. Ideally, never at all.
    */
  def bimapWeak[T, R](r: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] = {
    r.attempt.flatMap {
      case Left(t)  => Task.raiseError(bad(t))
      case Right(v) => Task.pure(good(v))
    }
  }

  def morph[T, R](io: Task[T], good: T => R, bad: Throwable => R): Task[R] =
    io.attempt.flatMap {
      case Left(value)  => Task.pure(bad(value))
      case Right(value) => Task.pure(good(value))
    }
}
