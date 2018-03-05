package busymachines.effects.async

import monix.{execution => mex}
import monix.{eval => mev}

import cats.{data => cd}

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.sync.validated._

import busymachines.duration, duration.FiniteDuration

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait TaskTypeDefinitions {

  final type CancellableFuture[T] = mex.CancelableFuture[T]

  /**
    * N.B.
    * that Scheduler is also a [[scala.concurrent.ExecutionContext]],
    * which makes this type the only implicit in context necessary to do
    * interop between [[Task]] and [[scala.concurrent.Future]]
    */
  final type Scheduler = mex.Scheduler
  final type Task[T]   = mev.Task[T]

  @inline final def Scheduler: mex.Scheduler.type = mex.Scheduler
  @inline final def Task:      mev.Task.type      = mev.Task

}

object TaskSyntax {

  /**
    *
    */
  trait Implicits {
    implicit final def bmcTaskCompanionObjectOps(obj: mev.Task.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcTaskReferenceOps[T](value: Task[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcTaskNestedOptionOps[T](nopt: Task[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit final def bmcTaskNestedResultOps[T](result: Task[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit final def bmcTaskBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit final def bmcTaskNestedBooleanOps(test: Task[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: mev.Task.type) extends AnyVal {

    // —— def pure[T](value: T): Task[T] —— already defined on companion object

    /**
      * Failed effect but with an [[Anomaly]]
      */

    @inline def fail[T](bad: Anomaly): Task[T] =
      TaskOps.fail(bad)

    /**
      * Failed effect but with a [[Throwable]]
      */

    @inline def failThr[T](bad: Throwable): Task[T] =
      TaskOps.failThr(bad)

    // —— def unit: Task[Unit] —— already defined on Task object

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.fromOption(opt, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[None]] then we get back a failed effect with the given [[Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): Task[T] =
      TaskOps.suspendOption(opt, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.fromOptionThr(opt, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[None]] then we get back a failed effect with the given [[Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromOption]]
      */
    @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): Task[T] =
      TaskOps.suspendOptionThr(opt, ifNone)

    // def fromTry[T](tr: Try[T]): Task[T] —— already defined on Task object

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[Try]] was previously assigned to a "val".
      * You might as well use [[Task.fromTry]]
      */
    @inline def suspendTry[T](tr: => Try[T]): Task[T] =
      TaskOps.suspendTry(tr)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Task[R] =
      TaskOps.fromEither(either, transformLeft)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromEither]]
      */
    @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): Task[R] =
      TaskOps.suspendEither(either, transformLeft)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[Throwable]] within this effect
      * if it is a [[Throwable]].
      */
    @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.fromEitherThr(either)(ev)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And sequence its left-hand-side [[Throwable]] within this effect if it is a [[Throwable]]
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
      TaskOps.suspendEitherThr(either)(ev)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Task[R] =
      TaskOps.fromEitherThr(either, transformLeft)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): Task[R] =
      TaskOps.suspendEitherThr(either, transformLeft)

    /**
      *
      * Lift the [[Result]] in this effect
      * [[Incorrect]] becomes a failed effect
      * [[Correct]] becomes a pure effect
      *
      */
    @inline def fromResult[T](result: Result[T]): Task[T] =
      TaskOps.fromResult(result)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Result]].
      * Other than that it has the semantics of [[TaskOps.fromResult]]
      *
      * N.B. this is useless if the [[Result]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromResult]]
      */
    @inline def suspendResult[T](result: => Result[T]): Task[T] =
      TaskOps.suspendResult(result)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      * Consider using the overload with an extra constructor parameter
      * for a custom [[busymachines.core.Anomalies]], otherwise your
      * all failed cases will be wrapped in a:
      * [[busymachines.effects.sync.validated.GenericValidationFailures]]
      */

    @inline def fromValidated[T](value: Validated[T]): Task[T] =
      TaskOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated.Invalid]] becomes a failed effect
      * [[Validated.Valid]] becomes a pure effect
      *
      * Provide the constructor for the specific [[busymachines.core.Anomalies]]
      * into which the anomalies shall be stored.
      *
      * e.g. Creating case classes like bellow, or constructors on companion objects
      * makes using this method almost completely non-intrusive
      * {{{
      * case class TVFs(
      *   bad:  Anomaly,
      *   bads: List[Anomaly] = Nil
      * ) extends AnomalousFailures(
      *       TVFsID,
      *       s"Test validation failed with ${bads.length + 1} anomalies",
      *       bad,
      *       bads
      *     )
      *
      * case object TVFsID extends AnomalyID {
      *   override def name = "test_validation_001"
      * }
      *
      * object Test {
      *   Task.fromValidated(
      *     Validated.pure(42),
      *     TVFs
      *   )
      *   //in validated postfix notation it's infinitely more concise
      *   Validated.pure(42).asTask(TVFs)
      * }
      * }}}
      *
      */

    @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.fromValidated(value, ctor)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[TaskOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T]): Task[T] =
      TaskOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
      TaskOps.suspendValidated(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * In 99% of the cases you actually want to use [[TaskOps.suspendFuture]]
      *
      * If you are certain that this [[Future]] is pure, then you can use
      * this method to lift it into [[Task]].
      */
    @inline def fromFuturePure[T](value: Future[T]): Task[T] =
      Task.fromFuture(value)

    /**
      *
      * Suspend the side-effects of this [[Future]] into a [[Task]]. This is the
      * most important operation when it comes to inter-op between the two effects.
      *
      * Usage. N.B. that this only makes sense if the creation of the Future itself
      * is also suspended in the [[Task]].
      * {{{
      * @inline def  writeToDB(v: Int, s: String): Future[Long] = ???
      *   //...
      *   val task = Task.suspendFuture(writeToDB(42, "string"))
      *   //no database writes happened yet, since the future did
      *   //not do its annoying running of side-effects immediately!
      *
      *   //when we want side-effects:
      *   task.unsafeGetSync()
      * }}}
      *
      * This is almost useless unless you are certain that ??? is a pure computation
      * might as well use Task.fromFuturePure(???)
      * {{{
      *   val f: Future[Int] = Future.apply(???)
      *   Task.suspendFuture(f)
      * }}}
      *
      */
    @inline def suspendFuture[T](result: => Future[T]): Task[T] =
      TaskOps.suspendFuture(result)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      */
    @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): Task[T] =
      TaskOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      */
    @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): Task[T] =
      TaskOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      */
    @inline def condWith[T](test: Boolean, good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      */
    @inline def condWithThr[T](test: Boolean, good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.condWithThr(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCond[T](test: Task[Boolean], good: => T, bad: => Anomaly): Task[T] =
      TaskOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondThr[T](test: Task[Boolean], good: => T, bad: => Throwable): Task[T] =
      TaskOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWith[T](test: Task[Boolean], good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWithThr[T](test: Task[Boolean], good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrue(test: Boolean, bad: => Anomaly): Task[Unit] =
      TaskOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueThr(test: Boolean, bad: => Throwable): Task[Unit] =
      TaskOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalse(test: Boolean, bad: => Anomaly): Task[Unit] =
      TaskOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseThr(test: Boolean, bad: => Throwable): Task[Unit] =
      TaskOps.failOnFalseThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrue(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrueThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalse(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalseThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnFalseThr(test, bad)

    /**
      * Sequences the given [[Anomaly]] if Option is [[None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOption[T](nopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
      TaskOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[Throwable]] if Option is [[None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOptionThr[T](nopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
      TaskOps.unpackOptionThr(nopt, ifNone)

    /**
      * Sequences the failure of the [[Incorrect]] [[Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[Incorrect]] value.
      */
    @inline def unpackResult[T](value: Task[Result[T]]): Task[T] =
      TaskOps.unpackResult(value)

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[Incorrect]] value.
      */
    @inline def attemptResult[T](value: Task[T]): Task[Result[T]] =
      TaskOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * The moment you call this, the side-effects suspended in this [[IO]] start being
      * executed.
      */
    @inline def asFutureUnsafe[T](value: Task[T])(implicit sc: Scheduler): Future[T] =
      TaskOps.asFutureUnsafe(value)

    /**
      * No gotchas. Pure functional programming = <3
      */
    @inline def asIO[T](value: Task[T])(implicit sc: Scheduler): IO[T] =
      TaskOps.asIO(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet[T](
      value:  Task[T],
      atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration
    )(
      implicit
      sc: Scheduler
    ): T =
      TaskOps.unsafeSyncGet(value, atMost)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Boolean]] is ``true``
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrue(test: Boolean, effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnTrue(test, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Boolean]] is ``true``
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnTrue(test: Task[Boolean], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnTrue(test, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalse(test: Boolean, effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnFalse(test, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnFalse(test: Task[Boolean], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnFalse(test, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[None]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail[T](value: Option[T], effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnFail(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[None]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnNone[T](value: Task[Option[T]], effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnNone(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[Some]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure[T](value: Option[T], effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnPure(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[Some]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnSome[T](value: Task[Option[T]], effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnSome(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Result]] is [[Incorrect]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail[T](value: Result[T], effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.effectOnFail(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the boxed value of this [[Result]] is [[Incorrect]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnIncorrect[T](value: Task[Result[T]], effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnIncorrect(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the boxed value of this [[Result]] is [[Correct]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnCorrect[T](value: Task[Result[T]], effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnCorrect(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Result]] is [[Correct]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure[T](value: Result[T], effect: T => Task[_]): Task[Unit] =
      TaskOps.effectOnPure(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[T, R](value: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] =
      TaskOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[T, R](value: Task[T], result: Result[T] => Result[R]): Task[R] =
      TaskOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[Throwable]] instead of [[Anomaly]]
      */
    @inline def bimapThr[T, R](value: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] =
      TaskOps.bimapThr(value, good, bad)

    /**
      *
      * Given the basic two-pronged nature of this effect.
      * the ``good`` function transforms the underlying "pure" (correct, successful, etc) if that's the case.
      * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
      *
      * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
      * that does not short-circuit monadic flatMap chains.
      *
      * e.g:
      * {{{
      *   val f: Future[Int] = Future.fail(InvalidInputFailure)
      *   Future.morph(f, (i: Int) => i *2, (t: Throwable) => 42)
      * }}}
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[T, R](value: Task[T], good: T => R, bad: Throwable => R): Task[R] =
      TaskOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[T, R](value: Task[T], result: Result[T] => R): Task[R] =
      TaskOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent(value: Task[_]): Task[Unit] =
      TaskOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    /**
      * Similar to [[monix.eval.Task.traverse]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[monix.eval.Task.traverse]]
      *
      */
    def traverse_[A, B, M[X] <: TraversableOnce[X]](col: M[A])(fn: A => Task[B])(
      implicit
      cbf: CanBuildFrom[M[A], B, M[B]]
    ): Task[Unit] = TaskOps.traverse_(col)(fn)

    /**
      * Similar to [[monix.eval.Task.sequence]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[monix.eval.Task.sequence]]
      *
      */
    @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Task[A]])(
      implicit
      cbf: CanBuildFrom[M[Task[A]], A, M[A]]
    ): Task[Unit] = TaskOps.sequence_(in)

    /**
      *
      * Syntactically inspired from [[Future.traverse]].
      *
      * See [[FutureOps.serialize]] for semantics.
      *
      * Usage:
      * {{{
      *   import busymachines.effects.async._
      *   val patches: Seq[Patch] = //...
      *
      *   //this ensures that no two changes will be applied in parallel.
      *   val allPatches: Task[Seq[Patch]] = Task.serialize(patches){ patch: Patch =>
      *     Task {
      *       //apply patch
      *     }
      *   }
      *   //... and so on, and so on!
      * }}}
      *
      *
      */
    @inline def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): Task[C[B]] = TaskOps.serialize(col)(fn)

    /**
      * Similar to [[serialize]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[serialize]]
      *
      */
    @inline def serialize_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): Task[Unit] = TaskOps.serialize_(col)(fn)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Task[T]) extends AnyVal {

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[Incorrect]] value.
      */
    @inline def attempResult: Task[Result[T]] =
      TaskOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * The moment you call this, the side-effects suspended in this [[IO]] start being
      * executed.
      */
    @inline def asFutureUnsafe()(implicit sc: Scheduler): Future[T] =
      TaskOps.asFutureUnsafe(value)

    /**
      * No gotchas. Pure functional programming = <3
      */
    @inline def asIO(implicit sc: Scheduler): IO[T] =
      TaskOps.asIO(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet(atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration)(
      implicit
      sc: Scheduler
    ): T =
      TaskOps.unsafeSyncGet(value, atMost)

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[R](good: T => R, bad: Throwable => Anomaly): Task[R] =
      TaskOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[R](result: Result[T] => Result[R]): Task[R] =
      TaskOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[Throwable]] instead of [[Anomaly]]
      */
    @inline def bimapThr[R](good: T => R, bad: Throwable => Throwable): Task[R] =
      TaskOps.bimapThr(value, good, bad)

    /**
      *
      * Given the basic two-pronged nature of this effect.
      * the ``good`` function transforms the underlying "pure" (correct, successful, etc) if that's the case.
      * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
      *
      * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
      * that does not short-circuit monadic flatMap chains.
      *
      * e.g:
      * {{{
      *   val f: Future[Int] = Future.fail(InvalidInputFailure)
      *   Future.morph(f, (i: Int) => i *2, (t: Throwable) => 42)
      * }}}
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[R](good: T => R, bad: Throwable => R): Task[R] =
      TaskOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[R](result: Result[T] => R): Task[R] =
      TaskOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent: Task[Unit] =
      TaskOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](val nopt: Task[Option[T]]) extends AnyVal {

    /**
      * Sequences the given [[Anomaly]] if Option is [[None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpack(ifNone: => Anomaly): Task[T] =
      TaskOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[Throwable]] if Option is [[None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackThr(ifNone: => Throwable): Task[T] =
      TaskOps.unpackOptionThr(nopt, ifNone)

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[None]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail(effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnNone(nopt, effect)

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[Some]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure(effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](val result: Task[Result[T]]) extends AnyVal {

    /**
      * Sequences the failure of the [[Incorrect]] [[Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[Incorrect]] value.
      */
    @inline def unpack: Task[T] =
      TaskOps.unpackResult(result)

    /**
      *
      * Runs the given effect when the boxed value of this [[Result]] is [[Incorrect]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail(effect: Anomaly => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnIncorrect(result, effect)

    /**
      *
      * Runs the given effect when the boxed value of this [[Result]] is [[Correct]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure(effect: T => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnCorrect(result, effect)
  }

  /**
    *
    *
    */
  final class BooleanOps(val test: Boolean) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      */
    @inline def condTask[T](good: => T, bad: => Anomaly): Task[T] =
      TaskOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      */
    @inline def condTaskThr[T](good: => T, bad: => Throwable): Task[T] =
      TaskOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      */
    @inline def condWithTask[T](good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      */
    @inline def condWithTaskThr[T](good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.condWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueTask(bad: => Anomaly): Task[Unit] =
      TaskOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueTaskThr(bad: => Throwable): Task[Unit] =
      TaskOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseTask(bad: => Anomaly): Task[Unit] =
      TaskOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseTaskThr(bad: => Throwable): Task[Unit] =
      TaskOps.failOnFalseThr(test, bad)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``false``
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalseTask(effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnFalse(test, effect)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``true``
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrueTask(effect: => Task[_]): Task[Unit] =
      TaskOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(val test: Task[Boolean]) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def cond[T](good: => T, bad: => Anomaly): Task[T] =
      TaskOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condThr[T](good: => T, bad: => Throwable): Task[T] =
      TaskOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWith[T](good: => Task[T], bad: => Anomaly): Task[T] =
      TaskOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWithThr[T](good: => Task[T], bad: => Throwable): Task[T] =
      TaskOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrue(bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrueThr(bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def failOnFalse(bad: => Anomaly): Task[Unit] =
      TaskOps.flatFailOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def failOnFalseThr(bad: => Throwable): Task[Unit] =
      TaskOps.flatFailOnFalseThr(test, bad)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``false``
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalse(effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnFalse(test, effect)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``true``
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrue(effect: => Task[_]): Task[Unit] =
      TaskOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object TaskOps {
  import cats.syntax.applicativeError._
  import cats.syntax.monadError._

  /**
    * N.B. pass only pure values. If you have side effects, then
    * use [[Task.apply]] to suspend them inside this future.
    */

  @inline def pure[T](value: T): Task[T] =
    Task.pure(value)

  /**
    * Failed effect but with an [[Anomaly]]
    */

  @inline def fail[T](bad: Anomaly): Task[T] =
    Task.raiseError(bad.asThrowable)

  /**
    * Failed effect but with a [[Throwable]]
    */

  @inline def failThr[T](bad: Throwable): Task[T] =
    Task.raiseError(bad)

  // —— def unit: Task[Unit] —— already defined on Task object

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): Task[T] = opt match {
    case None        => TaskOps.fail(ifNone)
    case Some(value) => TaskOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[None]] then we get back a failed effect with the given [[Anomaly]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromOption]]
    */
  @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): Task[T] =
    Task.suspend(TaskOps.fromOption(opt, ifNone))

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Task[T] = opt match {
    case None        => TaskOps.failThr(ifNone)
    case Some(value) => TaskOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[None]] then we get back a failed effect with the given [[Throwable]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromOption]]
    */
  @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): Task[T] =
    Task.suspend(TaskOps.fromOptionThr(opt, ifNone))

  // def fromTry[T](tr: Try[T]): Task[T] —— already defined on Task object

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Try]].
    * Failed Try yields a failed effect
    * Successful Try yields a pure effect
    *
    * N.B. this is useless if the [[Try]] was previously assigned to a "val".
    * You might as well use [[Task.fromTry]]
    */
  @inline def suspendTry[T](tr: => Try[T]): Task[T] =
    Task.suspend(Task.fromTry(tr))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Task[R] = either match {
    case Left(value)  => TaskOps.fail(transformLeft(value))
    case Right(value) => TaskOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromEither]]
    */
  @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): Task[R] =
    Task.suspend(TaskOps.fromEither(either, transformLeft))

  /**
    * Lift this [[Either]] and  sequence its left-hand-side [[Throwable]] within this effect
    * if it is a [[Throwable]].
    */
  @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Task[R] = either match {
    case Left(value)  => TaskOps.failThr(ev(value))
    case Right(value) => TaskOps.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And sequence its left-hand-side [[Throwable]] within this effect if it is a [[Throwable]]
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): Task[R] =
    Task.suspend(TaskOps.fromEitherThr(either)(ev))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Task[R] = either match {
    case Left(value)  => TaskOps.failThr(transformLeft(value))
    case Right(value) => TaskOps.pure(value)
  }

  /**
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): Task[R] =
    Task.suspend(TaskOps.fromEitherThr(either, transformLeft))

  /**
    *
    * Lift the [[Result]] in this effect
    * [[Incorrect]] becomes a failed effect
    * [[Correct]] becomes a pure effect
    *
    */
  @inline def fromResult[T](result: Result[T]): Task[T] = result match {
    case Left(value)  => TaskOps.fail(value)
    case Right(value) => TaskOps.pure(value)
  }

  /**
    * Suspend any side-effects that might happen during the creation of this [[Result]].
    * Other than that it has the semantics of [[TaskOps.fromResult]]
    *
    * N.B. this is useless if the [[Result]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromResult]]
    */
  @inline def suspendResult[T](result: => Result[T]): Task[T] =
    Task.suspend(TaskOps.fromResult(result))

  /**
    *
    * Lift the [[Validated]] in this effect
    * [[Validated.Invalid]] becomes a failed effect
    * [[Validated.Valid]] becomes a pure effect
    *
    * Consider using the overload with an extra constructor parameter
    * for a custom [[busymachines.core.Anomalies]], otherwise your
    * all failed cases will be wrapped in a:
    * [[busymachines.effects.sync.validated.GenericValidationFailures]]
    */

  @inline def fromValidated[T](value: Validated[T]): Task[T] = value match {
    case cd.Validated.Valid(e)   => TaskOps.pure(e)
    case cd.Validated.Invalid(e) => TaskOps.fail(GenericValidationFailures(e.head, e.tail))
  }

  /**
    *
    * Lift the [[Validated]] in this effect
    * [[Validated.Invalid]] becomes a failed effect
    * [[Validated.Valid]] becomes a pure effect
    *
    * Provide the constructor for the specific [[busymachines.core.Anomalies]]
    * into which the anomalies shall be stored.
    *
    * e.g. Creating case classes like bellow, or constructors on companion objects
    * makes using this method almost completely non-intrusive
    * {{{
    * case class TVFs(
    *   bad:  Anomaly,
    *   bads: List[Anomaly] = Nil
    * ) extends AnomalousFailures(
    *       TVFsID,
    *       s"Test validation failed with ${bads.length + 1} anomalies",
    *       bad,
    *       bads
    *     )
    *
    * case object TVFsID extends AnomalyID {
    *   override def name = "test_validation_001"
    * }
    *
    * object Test {
    *   Task.fromValidated(
    *     Validated.pure(42),
    *     TVFs
    *   )
    *   //in validated postfix notation it's infinitely more concise
    *   Validated.pure(42).asTask(TVFs)
    * }
    * }}}
    *
    */

  @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
    value match {
      case cd.Validated.Valid(e)   => TaskOps.pure(e)
      case cd.Validated.Invalid(e) => TaskOps.fail(ctor(e.head, e.tail))
    }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[TaskOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T]): Task[T] =
    Task.suspend(TaskOps.fromValidated(value))

  /**
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Task[T] =
    Task.suspend(TaskOps.fromValidated(value, ctor))

  /**
    * !!! USE WITH CARE !!!
    *
    * In 99% of the cases you actually want to use [[suspendFuture]]
    *
    * If you are certain that this [[Future]] is pure, then you can use
    * this method to lift it into [[Task]].
    */
  @inline def fromFuturePure[T](value: Future[T]): Task[T] =
    Task.fromFuture(value)

  /**
    *
    * Suspend the side-effects of this [[Future]] into a [[Task]]. This is the
    * most important operation when it comes to inter-op between the two effects.
    *
    * Usage. N.B. that this only makes sense if the creation of the Future itself
    * is also suspended in the [[Task]].
    * {{{
    * @inline def  writeToDB(v: Int, s: String): Future[Long] = ???
    *   //...
    *   val task = Task.suspendFuture(writeToDB(42, "string"))
    *   //no database writes happened yet, since the future did
    *   //not do its annoying running of side-effects immediately!
    *
    *   //when we want side-effects:
    *   task.unsafeGetSync()
    * }}}
    *
    * This is almost useless unless you are certain that ??? is a pure computation
    * might as well use Task.fromFuturePure(???)
    * {{{
    *   val f: Future[Int] = Future.apply(???)
    *   Task.suspendFuture(f)
    * }}}
    *
    */
  @inline def suspendFuture[T](value: => Future[T]): Task[T] =
    Task.deferFuture(value)

  /**
    *
    * Transform an [[IO]] into a [[Task]]. No gotchas because pure
    * functional programming is awesome.
    */
  @inline def fromIO[T](value: IO[T]): Task[T] =
    Task.fromIO(value)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): Task[T] =
    if (test) TaskOps.pure(good) else TaskOps.fail(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    */
  @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): Task[T] =
    if (test) TaskOps.pure(good) else TaskOps.failThr(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  @inline def condWith[T](test: Boolean, good: => Task[T], bad: => Anomaly): Task[T] =
    if (test) good else TaskOps.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    */
  @inline def condWithThr[T](test: Boolean, good: => Task[T], bad: => Throwable): Task[T] =
    if (test) good else TaskOps.failThr(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCond[T](test: Task[Boolean], good: => T, bad: => Anomaly): Task[T] =
    test.flatMap(t => TaskOps.cond(t, good, bad))

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondThr[T](test: Task[Boolean], good: => T, bad: => Throwable): Task[T] =
    test.flatMap(t => TaskOps.condThr(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWith[T](test: Task[Boolean], good: => Task[T], bad: => Anomaly): Task[T] =
    test.flatMap(t => TaskOps.condWith(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWithThr[T](test: Task[Boolean], good: => Task[T], bad: => Throwable): Task[T] =
    test.flatMap(t => TaskOps.condWithThr(t, good, bad))

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrue(test: Boolean, bad: => Anomaly): Task[Unit] =
    if (test) TaskOps.fail(bad) else Task.unit

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrueThr(test: Boolean, bad: => Throwable): Task[Unit] =
    if (test) TaskOps.failThr(bad) else Task.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalse(test: Boolean, bad: => Anomaly): Task[Unit] =
    if (!test) TaskOps.fail(bad) else Task.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalseThr(test: Boolean, bad: => Throwable): Task[Unit] =
    if (!test) TaskOps.failThr(bad) else Task.unit

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrue(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
    test.flatMap(t => TaskOps.failOnTrue(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrueThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
    test.flatMap(t => TaskOps.failOnTrueThr(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalse(test: Task[Boolean], bad: => Anomaly): Task[Unit] =
    test.flatMap(t => TaskOps.failOnFalse(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalseThr(test: Task[Boolean], bad: => Throwable): Task[Unit] =
    test.flatMap(t => TaskOps.failOnFalseThr(t, bad))

  /**
    * Sequences the given [[Anomaly]] if Option is [[None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOption[T](nopt: Task[Option[T]], ifNone: => Anomaly): Task[T] =
    nopt.flatMap {
      case None    => TaskOps.fail(ifNone)
      case Some(v) => TaskOps.pure(v)
    }

  /**
    * Sequences the given [[Throwable]] if Option is [[None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOptionThr[T](nopt: Task[Option[T]], ifNone: => Throwable): Task[T] =
    nopt.flatMap {
      case None    => TaskOps.failThr(ifNone)
      case Some(v) => TaskOps.pure(v)
    }

  /**
    * Sequences the failure of the [[Incorrect]] [[Result]] into this effect.
    *
    * The failure of this effect takes precedence over the failure of the [[Incorrect]] value.
    */
  @inline def unpackResult[T](value: Task[Result[T]]): Task[T] = value.flatMap {
    case Left(a)  => TaskOps.fail(a)
    case Right(a) => TaskOps.pure(a)
  }

  /**
    * Makes the failure, and non-failure part of this effect explicit in a [[Result]] type.
    *
    * This transforms any failed effect, into a pure one with and [[Incorrect]] value.
    */
  @inline def attemptResult[T](value: Task[T]): Task[Result[T]] =
    value.attempt.map((e: Either[Throwable, T]) => Result.fromEitherThr(e))

  /**
    * !!! USE WITH CARE !!!
    *
    * The moment you call this, the side-effects suspended in this [[IO]] start being
    * executed.
    */
  @inline def asFutureUnsafe[T](value: Task[T])(implicit sc: Scheduler): CancellableFuture[T] =
    value.runAsync

  /**
    * No gotchas. Pure functional programming = <3
    */
  @inline def asIO[T](value: Task[T])(implicit sc: Scheduler): IO[T] =
    value.toIO

  /**
    * !!! USE WITH CARE !!!
    *
    * Mostly here for testing. There is almost no reason whatsover for you to explicitely
    * call this in your code. You have libraries that do this for you "at the end of the world"
    * parts of your program: e.g. akka-http when waiting for the response value to a request.
    */
  @inline def unsafeSyncGet[T](
    value:  Task[T],
    atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration
  )(
    implicit sc: Scheduler
  ): T = value.runAsync.unsafeSyncGet(atMost)

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnTrue(test: Boolean, effect: => Task[_]): Task[Unit] =
    if (test) TaskOps.discardContent(effect) else Task.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnTrue(test: Task[Boolean], effect: => Task[_]): Task[Unit] =
    test.flatMap(t => TaskOps.effectOnTrue(t, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFalse(test: Boolean, effect: => Task[_]): Task[Unit] =
    if (!test) TaskOps.discardContent(effect) else Task.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnFalse(test: Task[Boolean], effect: => Task[_]): Task[Unit] =
    test.flatMap(t => TaskOps.effectOnFalse(t, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[None]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFail[T](value: Option[T], effect: => Task[_]): Task[Unit] =
    if (value.isEmpty) TaskOps.discardContent(effect) else Task.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[None]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnNone[T](value: Task[Option[T]], effect: => Task[_]): Task[Unit] =
    value.flatMap(opt => TaskOps.effectOnFail(opt, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[Some]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnPure[T](value: Option[T], effect: T => Task[_]): Task[Unit] =
    value match {
      case None    => Task.unit
      case Some(v) => TaskOps.discardContent(effect(v))

    }

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[Some]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnSome[T](value: Task[Option[T]], effect: T => Task[_]): Task[Unit] =
    value.flatMap(opt => TaskOps.effectOnPure(opt, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Result]] is [[Incorrect]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFail[T](value: Result[T], effect: Anomaly => Task[_]): Task[Unit] = value match {
    case Correct(_)         => Task.unit
    case Incorrect(anomaly) => TaskOps.discardContent(effect(anomaly))
  }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[Result]] is [[Incorrect]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnIncorrect[T](value: Task[Result[T]], effect: Anomaly => Task[_]): Task[Unit] =
    value.flatMap(result => TaskOps.effectOnFail(result, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Result]] is [[Correct]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnPure[T](value: Result[T], effect: T => Task[_]): Task[Unit] =
    value match {
      case Incorrect(_) => Task.unit
      case Correct(v)   => TaskOps.discardContent(effect(v))
    }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[Result]] is [[Correct]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnCorrect[T](value: Task[Result[T]], effect: T => Task[_]): Task[Unit] =
    value.flatMap(result => TaskOps.effectOnPure(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  /**
    * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
    * "bi" map, because it also allows you to change both branches of the effect, not just the
    * happy path.
    */
  @inline def bimap[T, R](value: Task[T], good: T => R, bad: Throwable => Anomaly): Task[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t).asThrowable
    }

  /**
    * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
    */
  @inline def bimap[T, R](value: Task[T], result: Result[T] => Result[R]): Task[R] =
    TaskOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => TaskOps.pure(v)
      case Incorrect(v) => TaskOps.fail(v)
    }

  /**
    * Similar to the overload, but the [[Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[Incorrect]] branch is used to change the "fail" branch of the effect.
    *
    * The overload that uses [[Throwable]] instead of [[Anomaly]]
    */
  @inline def bimapThr[T, R](value: Task[T], good: T => R, bad: Throwable => Throwable): Task[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t)
    }

  /**
    *
    * Given the basic two-pronged nature of this effect.
    * the ``good`` function transforms the underlying "pure" (correct, successful, etc) if that's the case.
    * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
    *
    * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
    * that does not short-circuit monadic flatMap chains.
    *
    * e.g:
    * {{{
    *   val f: Future[Int] = Future.fail(InvalidInputFailure)
    *   Future.morph(f, (i: Int) => i *2, (t: Throwable) => 42)
    * }}}
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  @inline def morph[T, R](value: Task[T], good: T => R, bad: Throwable => R): Task[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  /**
    * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
    * as the corresponding branches of a Result type.
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  @inline def morph[T, R](value: Task[T], result: Result[T] => R): Task[R] =
    TaskOps.attemptResult(value).map(result)

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. computation, and side-effects captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  @inline def discardContent(value: Task[_]): Task[Unit] =
    value.map(ConstantsAsyncEffects.UnitFunction1)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * Similar to [[monix.eval.Task.traverse]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[monix.eval.Task.traverse]]
    *
    */
  def traverse_[A, B, M[X] <: TraversableOnce[X]](col: M[A])(fn: A => Task[B])(
    implicit
    cbf: CanBuildFrom[M[A], B, M[B]]
  ): Task[Unit] = TaskOps.discardContent(Task.traverse(col)(fn))

  /**
    * Similar to [[monix.eval.Task.sequence]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[monix.eval.Task.sequence]]
    *
    */
  @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Task[A]])(
    implicit
    cbf: CanBuildFrom[M[Task[A]], A, M[A]]
  ): Task[Unit] = TaskOps.discardContent(Task.sequence(in))

  /**
    *
    * Syntactically inspired from [[Future.traverse]].
    *
    * See [[FutureOps.serialize]] for semantics.
    *
    * Usage:
    * {{{
    *   import busymachines.effects.async._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: Task[Seq[Patch]] = Task.serialize(patches){ patch: Patch =>
    *     Task {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  @inline def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): Task[C[B]] = Task.traverse(col)(fn)(cbf)

  /**
    * Similar to [[serialize]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[serialize]]
    *
    */
  @inline def serialize_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Task[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): Task[Unit] = TaskOps.discardContent(TaskOps.serialize(col)(fn))

}
