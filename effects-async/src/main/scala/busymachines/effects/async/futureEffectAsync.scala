package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.sync.validated._
import busymachines.duration.FiniteDuration
import cats.effect.ContextShift

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal
import scala.{concurrent => sc}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
trait FutureTypeDefinitions {
  final type Future[T] = sc.Future[T]
  @inline final def Future: sc.Future.type = sc.Future

  final type ExCtx            = sc.ExecutionContext
  final type ExecutionContext = sc.ExecutionContext

  @inline final def ExCtx:            sc.ExecutionContext.type = sc.ExecutionContext
  @inline final def ExecutionContext: sc.ExecutionContext.type = sc.ExecutionContext
  @inline final def Await:            sc.Await.type            = sc.Await
  @inline final def blocking[T](body: => T): T = sc.blocking(body)

}

object FutureSyntax {

  /**
    *
    */
  trait Implicits {
    implicit final def bmcFutureCompanionObjectOps(obj: sc.Future.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcFutureReferenceOps[T](value: Future[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcFutureSafeReferenceOps[T](value: => Future[T]): SafeReferenceOps[T] =
      new SafeReferenceOps(value)

    implicit final def bmcFutureNestedOptionOps[T](nopt: Future[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit final def bmcFutureNestedResultOps[T](result: Future[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit final def bmcFutureBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit final def bmcFutureNestedBooleanOps(test: Future[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: sc.Future.type) extends AnyVal {

    /**
      * N.B. pass only pure values. If you have side effects, then
      * use [[Future#apply]] to suspend them inside this future.
      */
    @inline def pure[T](value: T): Future[T] =
      FutureOps.pure(value)

    /**
      * Failed effect but with an [[busymachines.core.Anomaly]]
      */
    @inline def fail[T](bad: Anomaly): Future[T] =
      FutureOps.fail(bad)

    /**
      * Failed effect with a [[java.lang.Throwable]]
      */
    @inline def failThr[T](bad: Throwable): Future[T] =
      FutureOps.failThr(bad)

    // —— def unit: Future[Unit] —— already defined on Future object

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): Future[T] =
      FutureOps.fromOption(opt, ifNone)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOption(opt, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect, if it is [[scala.None]]
      */
    @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Future[T] =
      FutureOps.fromOptionThr(opt, ifNone)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromOption]]
      */
    @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendOptionThr(opt, ifNone)

    // —— def fromTry —— already defined on Future object

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[Future#fromTry]]
      */
    @inline def suspendTry[T](tr: => Try[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendTry(tr)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Future[R] =
      FutureOps.fromEither(either, transformLeft)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly)(
      implicit ec:                          ExecutionContext,
    ): Future[R] = FutureOps.suspendEither(either, transformLeft)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
      * if it is a [[java.lang.Throwable]].
      */
    @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Future[R] =
      FutureOps.fromEitherThr(either)(ev)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R])(
      implicit
      ev: L <:< Throwable,
      ec: ExecutionContext,
    ): Future[R] = FutureOps.suspendEitherThr(either)(ev, ec)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Future[R] =
      FutureOps.fromEitherThr(either, transformLeft)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable)(
      implicit ec:                             ExecutionContext,
    ): Future[R] = FutureOps.suspendEitherThr(either, transformLeft)

    /**
      *
      * Lift the [[busymachines.effects.sync.Result]] in this effect
      * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
      * [[busymachines.effects.sync.Correct]] becomes a pure effect
      *
      */
    @inline def fromResult[T](result: Result[T]): Future[T] =
      FutureOps.fromResult(result)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
      *
      * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromResult]]
      */
    @inline def suspendResult[T](result: => Result[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendResult(result)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated#Invalid]] becomes a failed effect
      * [[Validated#Valid]] becomes a pure effect
      *
      * Consider using the overload with an extra constructor parameter
      * for a custom [[busymachines.core.Anomalies]], otherwise your
      * all failed cases will be wrapped in a:
      * [[busymachines.effects.sync.validated.GenericValidationFailures]]
      */
    @inline def fromValidated[T](value: Validated[T]): Future[T] =
      FutureOps.fromValidated(value)

    /**
      *
      * Lift the [[Validated]] in this effect
      * [[Validated#Invalid]] becomes a failed effect
      * [[Validated#Valid]] becomes a pure effect
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
      *   Future.fromValidated(
      *     Validated.pure(42),
      *     TVFs
      *   )
      *   //in validated postfix notation it's infinitely more concise
      *   Validated.pure(42).asFuture(TVFs)
      * }
      * }}}
      *
      */
    @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
      FutureOps.fromValidated(value, ctor)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.suspendValidated(value)

    /**
      * N.B.
      * For Future in particular, this is useless, since you suspend a side-effect which
      * gets immediately applied due to the nature of the Future. This is useful only that
      * any exceptions thrown (bad code) is captured "within" the Future.
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies)(
      implicit
      ec: ExecutionContext,
    ): Future[T] = FutureOps.suspendValidated(value, ctor)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): Future[T] =
      FutureOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): Future[T] =
      FutureOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWith[T](test: Boolean, good: => Future[T], bad: => Anomaly): Future[T] =
      FutureOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condWithThr[T](test: Boolean, good: => Future[T], bad: => Throwable): Future[T] =
      FutureOps.condWithThr(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCond[T](test: Future[Boolean], good: => T, bad: => Anomaly)(
      implicit
      ec: ExecutionContext,
    ): Future[T] =
      FutureOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondThr[T](test: Future[Boolean], good: => T, bad: => Throwable)(
      implicit
      ec: ExecutionContext,
    ): Future[T] =
      FutureOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWith[T](test: Future[Boolean], good: => Future[T], bad: => Anomaly)(
      implicit
      ec: ExecutionContext,
    ): Future[T] =
      FutureOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWithThr[T](
      test: Future[Boolean],
      good: => Future[T],
      bad:  => Throwable,
    )(
      implicit ec: ExecutionContext,
    ): Future[T] =
      FutureOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrue(test: Boolean, bad: => Anomaly): Future[Unit] =
      FutureOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueThr(test: Boolean, bad: => Throwable): Future[Unit] =
      FutureOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalse(test: Boolean, bad: => Anomaly): Future[Unit] =
      FutureOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseThr(test: Boolean, bad: => Throwable): Future[Unit] =
      FutureOps.failOnFalseThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrue(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrueThr(test: Future[Boolean], bad: => Throwable)(
      implicit
      ec: ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalse(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalseThr(test: Future[Boolean], bad: => Throwable)(
      implicit
      ec: ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatFailOnFalseThr(test, bad)

    /**
      * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOption[T](nopt: Future[Option[T]], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOptionThr[T](nopt: Future[Option[T]], ifNone: => Throwable)(
      implicit
      ec: ExecutionContext,
    ): Future[T] =
      FutureOps.unpackOptionThr(nopt, ifNone)

    /**
      * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def unpackResult[T](value: Future[Result[T]])(implicit ec: ExecutionContext): Future[T] =
      FutureOps.unpackResult(value)

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def attemptResult[T](value: Future[T])(implicit ec: ExecutionContext): Future[Result[T]] =
      FutureOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      * Most likely you want to use [[FutureOps.suspendInIO]] which ensures that it suspends
      * the side effects of the given future (if it created by the given expression,
      * if it's a val, then good luck).
      *
      * Having to care about such val/def distinctions shows why Future is an
      * imperative programming mess.
      */
    @inline def asIO[T](value: Future[T]): IO[T] =
      FutureOps.asIO(value)

    /**
      *
      * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
      * most important operation when it comes to inter-op between the two effects.
      *
      * Usage. N.B. that this only makes sense if the creation of the Future itself
      * is also suspended in the [[IO]].
      * {{{
      * @inline def  writeToDB(v: Int, s: String): Future[Long] = ???
      *   //...
      *   val io = IO.suspendFuture(writeToDB(42, "string"))
      *   //no database writes happened yet, since the future did
      *   //not do its annoying running of side-effects immediately!
      *
      *   //when we want side-effects:
      *   io.unsafeGetSync()
      * }}}
      *
      * This is almost useless unless you are certain that ??? is a pure computation
      * might as well use IO.fromFuturePure(???)
      * {{{
      *   val f: Future[Int] = Future.apply(???)
      *   IO.suspendFuture(f)
      * }}}
      *
      */
    @inline def suspendInIO[T](value: => Future[T])(implicit cs: ContextShift[IO]): IO[T] =
      FutureOps.suspendInIO(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet[T](value: Future[T], atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration): T =
      FutureOps.unsafeSyncGet(value, atMost)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``true``
      *   Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrue(test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnTrue(test, effect)

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``true``
      *   Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnTrue(test: Future[Boolean], effect: => Future[_])(
      implicit
      ec: ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnTrue(test, effect)

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalse(test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFalse(test, effect)

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      *   Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnFalse(test: Future[Boolean], effect: => Future[_])(
      implicit
      ec: ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnFalse(test, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[scala.None]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail[T](value: Option[T], effect: => Future[_])(
      implicit ec:                     ExecutionContext,
    ): Future[Unit] =
      FutureOps.effectOnFail(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[Option]] is [[scala.None]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnNone[T](value: Future[Option[T]], effect: => Future[_])(
      implicit ec:                         ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnNone(value, effect)

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
    @inline def effectOnPure[T](value: Option[T], effect: T => Future[_])(
      implicit ec:                     ExecutionContext,
    ): Future[Unit] =
      FutureOps.effectOnPure(value, effect)

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
    @inline def flatEffectOnSome[T](value: Future[Option[T]], effect: T => Future[_])(
      implicit ec:                         ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnSome(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail[T](value: Result[T], effect: Anomaly => Future[_])(
      implicit ec:                     ExecutionContext,
    ): Future[Unit] =
      FutureOps.effectOnFail(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnIncorrect[T](value: Future[Result[T]], effect: Anomaly => Future[_])(
      implicit ec:                              ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnIncorrect(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      *   Does not run the side-effect if the value is also a failed effect.
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def flatEffectOnCorrect[T](value: Future[Result[T]], effect: T => Future[_])(
      implicit ec:                            ExecutionContext,
    ): Future[Unit] =
      FutureOps.flatEffectOnCorrect(value, effect)

    /**
      *
      * @param value
      *   Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure[T](value: Result[T], effect: T => Future[_])(
      implicit ec:                     ExecutionContext,
    ): Future[Unit] =
      FutureOps.effectOnPure(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[T, R](value: Future[T], good: T => R, bad: Throwable => Anomaly)(
      implicit ec:                 ExecutionContext,
    ): Future[R] =
      FutureOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[T, R](value: Future[T], result: Result[T] => Result[R])(
      implicit ec:                 ExecutionContext,
    ): Future[R] =
      FutureOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
      */
    @inline def bimapThr[T, R](value: Future[T], good: T => R, bad: Throwable => Throwable)(
      implicit ec:                    ExecutionContext,
    ): Future[R] =
      FutureOps.bimapThr(value, good, bad)

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
    @inline def morph[T, R](value: Future[T], good: T => R, bad: Throwable => R)(
      implicit ec:                 ExecutionContext,
    ): Future[R] =
      FutureOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[T, R](value: Future[T], result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent(value: Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    /**
      *
      * Similar to [[scala.concurrent.Future.traverse]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[scala.concurrent.Future.traverse]]
      */
    @inline def traverse_[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: A => Future[B])(
      implicit
      cbf: CanBuildFrom[M[A], B, M[B]],
      ec:  ExecutionContext,
    ): Future[Unit] = FutureOps.traverse_(in)(fn)

    /**
      *
      * Similar to [[scala.concurrent.Future.sequence]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[scala.concurrent.Future.sequence]]
      */
    @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(
      implicit
      cbf: CanBuildFrom[M[Future[A]], A, M[A]],
      ec:  ExecutionContext,
    ): Future[Unit] = FutureOps.sequence_(in)

    /**
      *
      * Syntactically inspired from [[Future#traverse]], but it differs semantically
      * insofar as this method does not attempt to run any futures in parallel. "M" stands
      * for "monadic", as opposed to "applicative" which is the foundation for the formal definition
      * of "traverse" (even though in Scala it is by accident-ish)
      *
      * For the vast majority of cases you should prefer this method over [[Future#sequence]]
      * and [[Future#traverse]], since even small collections can easily wind up queuing so many
      * [[Future]]s that you blow your execution context.
      *
      * Usage:
      * {{{
      *   import busymachines.effects.async._
      *   val patches: Seq[Patch] = //...
      *
      *   //this ensures that no two changes will be applied in parallel.
      *   val allPatches: Future[Seq[Patch]] = Future.serialize(patches){ patch: Patch =>
      *     Future {
      *       //apply patch
      *     }
      *   }
      *   //... and so on, and so on!
      * }}}
      *
      *
      */
    @inline def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]],
      ec:  ExecutionContext,
    ): Future[C[B]] = FutureOps.serialize(col)(fn)

    /**
      * @see [[serialize]]
      *
      * Similar to [[serialize]], but discards all content. i.e. used only
      * for the combined effects.
      */
    @inline def serialize_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]],
      ec:  ExecutionContext,
    ): Future[Unit] = FutureOps.serialize_(col)(fn)
  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Future[T]) extends AnyVal {

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def attempResult(implicit ec: ExecutionContext): Future[Result[T]] =
      FutureOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      * Most likely you want to use [[FutureOps.suspendInIO]] which ensures that it suspends
      * the side effects of the given future (if it created by the given expression,
      * if it's a val, then good luck).
      *
      * Having to care about such val/def distinctions shows why Future is an
      * imperative programming mess.
      */
    @inline def asIO: IO[T] =
      FutureOps.asIO(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet(atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration): T =
      FutureOps.unsafeSyncGet(value, atMost)

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[R](good: T => R, bad: Throwable => Anomaly)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[R](result: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
      */
    @inline def bimapThr[R](good: T => R, bad: Throwable => Throwable)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.bimapThr(value, good, bad)

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
    @inline def morph[R](good: T => R, bad: Throwable => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[R](result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
      FutureOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.discardContent(value)
  }

  /**
    *
    */
  final class SafeReferenceOps[T](value: => Future[T]) {

    /**
      *
      * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
      * most important operation when it comes to inter-op between the two effects.
      *
      * Usage. N.B. that this only makes sense if the creation of the Future itself
      * is also suspended in the [[IO]].
      * {{{
      * @inline def  writeToDB(v: Int, s: String): Future[Long] = ???
      *   //...
      *   val io = IO.suspendFuture(writeToDB(42, "string"))
      *   //no database writes happened yet, since the future did
      *   //not do its annoying running of side-effects immediately!
      *
      *   //when we want side-effects:
      *   io.unsafeGetSync()
      * }}}
      *
      * This is almost useless unless you are certain that ??? is a pure computation
      * might as well use IO.fromFuturePure(???)
      * {{{
      *   val f: Future[Int] = Future.apply(???)
      *   IO.suspendFuture(f)
      * }}}
      *
      */
    @inline def suspendInIO(implicit cs: ContextShift[IO]): IO[T] =
      FutureOps.suspendInIO(value)

  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](val nopt: Future[Option[T]]) extends AnyVal {

    /**
      * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpack(ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackThr(ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.unpackOptionThr(nopt, ifNone)

    /**
      *
      * Runs the given effect when the value of this [[Option]] is [[scala.None]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnNone(nopt, effect)

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
    @inline def effectOnPure(effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](val result: Future[Result[T]]) extends AnyVal {

    /**
      * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def unpack(implicit ec: ExecutionContext): Future[T] =
      FutureOps.unpackResult(result)

    /**
      *
      * Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFail(effect: Anomaly => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnIncorrect(result, effect)

    /**
      *
      * Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnPure(effect: T => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnCorrect(result, effect)
  }

  /**
    *
    *
    */
  final class BooleanOps(val test: Boolean) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condFuture[T](good: => T, bad: => Anomaly): Future[T] =
      FutureOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condFutureThr[T](good: => T, bad: => Throwable): Future[T] =
      FutureOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWithFuture[T](good: => Future[T], bad: => Anomaly): Future[T] =
      FutureOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condWithFutureThr[T](good: => Future[T], bad: => Throwable): Future[T] =
      FutureOps.condWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueFuture(bad: => Anomaly): Future[Unit] =
      FutureOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueFutureThr(bad: => Throwable): Future[Unit] =
      FutureOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseFuture(bad: => Anomaly): Future[Unit] =
      FutureOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseFutureThr(bad: => Throwable): Future[Unit] =
      FutureOps.failOnFalseThr(test, bad)

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalseFuture(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnFalse(test, effect)

    /**
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``true``
      *   Does not run the side-effect if the value is also a failed effect.
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrueFuture(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(val test: Future[Boolean]) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def cond[T](good: => T, bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condThr[T](good: => T, bad: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWith[T](good: => Future[T], bad: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWithThr[T](good: => Future[T], bad: => Throwable)(implicit ec: ExecutionContext): Future[T] =
      FutureOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrue(bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrueThr(bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def failOnFalse(bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalse(test, bad)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``false``
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def failOnFalseThr(bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatFailOnFalseThr(test, bad)

    @inline def effectOnFalse(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnFalse(test, effect)

    /**
      *
      * Runs the given effect when the value of this [[Boolean]] is ``true``
      * Does not run the side-effect if the value is also a failed effect.
      *
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnTrue(effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
      FutureOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object FutureOps {

  /**
    * N.B. pass only pure values. If you have side effects, then
    * use [[Future#apply]] to suspend them inside this future.
    */

  @inline def pure[T](value: T): Future[T] =
    Future.successful(value)

  /**
    * Failed effect but with an [[busymachines.core.Anomaly]]
    */

  @inline def fail[T](bad: Anomaly): Future[T] =
    Future.failed(bad.asThrowable)

  /**
    * Failed effect with a [[java.lang.Throwable]]
    */

  @inline def failThr[T](bad: Throwable): Future[T] =
    Future.failed(bad)

  // —— def unit: Future[Unit] —— already defined on Future object

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */

  @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): Future[T] = opt match {
    case None        => FutureOps.fail(ifNone)
    case Some(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromOption]]
    */
  @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    Future(opt).flatMap(o => FutureOps.fromOption(o, ifNone))

  /**
    * Lift this [[Option]] and transform it into a failed effect, if it is [[scala.None]]
    */
  @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): Future[T] = opt match {
    case None        => FutureOps.failThr(ifNone)
    case Some(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromOption]]
    */
  @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable)(implicit ec: ExecutionContext): Future[T] =
    Future(opt).flatMap(o => FutureOps.fromOptionThr(o, ifNone))

  // —— def fromTry[T](tr: Try[T]): Future[T] —— already exists on Future

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
    * Failed Try yields a failed effect
    * Successful Try yields a pure effect
    *
    * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
    * You might as well use [[Future#fromTry]]
    */
  @inline def suspendTry[T](tr: => Try[T])(implicit ec: ExecutionContext): Future[T] =
    Future(tr).flatMap(Future.fromTry)

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): Future[R] = either match {
    case Left(value)  => FutureOps.fail(transformLeft(value))
    case Right(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromEither]]
    */
  @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly)(
    implicit ec:                          ExecutionContext,
  ): Future[R] =
    Future(either).flatMap(eit => FutureOps.fromEither(eit, transformLeft))

  /**
    * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
    * if it is a [[java.lang.Throwable]].
    */
  @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): Future[R] = either match {
    case Left(value)  => FutureOps.failThr(ev(value))
    case Right(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](
    either:      => Either[L, R],
  )(implicit ev: L <:< Throwable, ec: ExecutionContext): Future[R] =
    Future(either).flatMap(eit => FutureOps.fromEitherThr(eit)(ev))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): Future[R] = either match {
    case Left(value)  => FutureOps.failThr(transformLeft(value))
    case Right(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable)(
    implicit ec:                             ExecutionContext,
  ): Future[R] = Future(either).flatMap(eit => FutureOps.fromEitherThr(eit, transformLeft))

  /**
    *
    * Lift the [[busymachines.effects.sync.Result]] in this effect
    * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
    * [[busymachines.effects.sync.Correct]] becomes a pure effect
    *
    */
  @inline def fromResult[T](result: Result[T]): Future[T] = result match {
    case Left(value)  => FutureOps.fail(value)
    case Right(value) => FutureOps.pure(value)
  }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
    *
    * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromResult]]
    */
  @inline def suspendResult[T](result: => Result[T])(implicit ec: ExecutionContext): Future[T] =
    Future(result).flatMap(FutureOps.fromResult)

  /**
    *
    * Lift the [[Validated]] in this effect
    * [[Validated#Invalid]] becomes a failed effect
    * [[Validated#Valid]] becomes a pure effect
    *
    * Consider using the overload with an extra constructor parameter
    * for a custom [[busymachines.core.Anomalies]], otherwise your
    * all failed cases will be wrapped in a:
    * [[busymachines.effects.sync.validated.GenericValidationFailures]]
    */

  @inline def fromValidated[T](value: Validated[T]): Future[T] = value match {
    case cats.data.Validated.Valid(e)   => FutureOps.pure(e)
    case cats.data.Validated.Invalid(e) => FutureOps.fail(GenericValidationFailures(e.head, e.tail))
  }

  /**
    *
    * Lift the [[Validated]] in this effect
    * [[Validated#Invalid]] becomes a failed effect
    * [[Validated#Valid]] becomes a pure effect
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
    *   Future.fromValidated(
    *     Validated.pure(42),
    *     TVFs
    *   )
    *   //in validated postfix notation it's infinitely more concise
    *   Validated.pure(42).asFuture(TVFs)
    * }
    * }}}
    *
    */

  @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Future[T] =
    value match {
      case cats.data.Validated.Valid(e)   => FutureOps.pure(e)
      case cats.data.Validated.Invalid(e) => FutureOps.fail(ctor(e.head, e.tail))
    }

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T])(implicit ec: ExecutionContext): Future[T] =
    Future(value).flatMap(v => FutureOps.fromValidated(v))

  /**
    * N.B.
    * For Future in particular, this is useless, since you suspend a side-effect which
    * gets immediately applied due to the nature of the Future. This is useful only that
    * any exceptions thrown (bad code) is captured "within" the Future.
    *
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    Future(value).flatMap(v => FutureOps.fromValidated(v, ctor))

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): Future[T] =
    if (test) FutureOps.pure(good) else FutureOps.fail(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    */
  @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): Future[T] =
    if (test) FutureOps.pure(good) else FutureOps.failThr(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def condWith[T](test: Boolean, good: => Future[T], bad: => Anomaly): Future[T] =
    if (test) good else FutureOps.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    */
  @inline def condWithThr[T](test: Boolean, good: => Future[T], bad: => Throwable): Future[T] =
    if (test) good else FutureOps.failThr(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCond[T](test: Future[Boolean], good: => T, bad: => Anomaly)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    test.flatMap(t => FutureOps.cond(t, good, bad))

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondThr[T](test: Future[Boolean], good: => T, bad: => Throwable)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    test.flatMap(t => FutureOps.condThr(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWith[T](test: Future[Boolean], good: => Future[T], bad: => Anomaly)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    test.flatMap(t => FutureOps.condWith(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWithThr[T](test: Future[Boolean], good: => Future[T], bad: => Throwable)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    test.flatMap(t => FutureOps.condWithThr(t, good, bad))

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrue(test: Boolean, bad: => Anomaly): Future[Unit] =
    if (test) FutureOps.fail(bad) else Future.unit

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrueThr(test: Boolean, bad: => Throwable): Future[Unit] =
    if (test) FutureOps.failThr(bad) else Future.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalse(test: Boolean, bad: => Anomaly): Future[Unit] =
    if (!test) FutureOps.fail(bad) else Future.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalseThr(test: Boolean, bad: => Throwable): Future[Unit] =
    if (!test) FutureOps.failThr(bad) else Future.unit

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrue(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnTrue(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrueThr(test: Future[Boolean], bad: => Throwable)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnTrueThr(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalse(test: Future[Boolean], bad: => Anomaly)(implicit ec: ExecutionContext): Future[Unit] =
    test.flatMap(t => FutureOps.failOnFalse(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalseThr(test: Future[Boolean], bad: => Throwable)(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    test.flatMap(t => FutureOps.failOnFalseThr(t, bad))

  /**
    * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOption[T](nopt: Future[Option[T]], ifNone: => Anomaly)(implicit ec: ExecutionContext): Future[T] =
    nopt.flatMap {
      case None    => FutureOps.fail(ifNone)
      case Some(v) => FutureOps.pure(v)
    }

  /**
    * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOptionThr[T](nopt: Future[Option[T]], ifNone: => Throwable)(
    implicit
    ec: ExecutionContext,
  ): Future[T] =
    nopt.flatMap {
      case None    => FutureOps.failThr(ifNone)
      case Some(v) => FutureOps.pure(v)
    }

  /**
    * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
    *
    * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
    */
  @inline def unpackResult[T](value: Future[Result[T]])(implicit ec: ExecutionContext): Future[T] = value.flatMap {
    case Left(a)  => FutureOps.fail(a)
    case Right(a) => FutureOps.pure(a)
  }

  /**
    * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
    *
    * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
    */
  @inline def attemptResult[T](value: Future[T])(implicit ec: ExecutionContext): Future[Result[T]] =
    value.map(Result.pure).recover {
      case NonFatal(t) => Result.failThr(t)
    }

  /**
    * !!! USE WITH CARE !!!
    * Most likely you want to use [[FutureOps.suspendInIO]] which ensures that it suspends
    * the side effects of the given future (if it created by the given expression,
    * if it's a val, then good luck).
    *
    * Having to care about such val/def distinctions shows why Future is an
    * imperative programming mess.
    */
  @inline def asIO[T](value: Future[T]): IO[T] =
    IOOps.fromFuturePure(value)

  /**
    *
    * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
    * most important operation when it comes to inter-op between the two effects.
    *
    * Usage. N.B. that this only makes sense if the creation of the Future itself
    * is also suspended in the [[IO]].
    * {{{
    * @inline def  writeToDB(v: Int, s: String): Future[Long] = ???
    *   //...
    *   val io = IO.suspendFuture(writeToDB(42, "string"))
    *   //no database writes happened yet, since the future did
    *   //not do its annoying running of side-effects immediately!
    *
    *   //when we want side-effects:
    *   io.unsafeGetSync()
    * }}}
    *
    * This is almost useless unless you are certain that ??? is a pure computation
    * might as well use IO.fromFuturePure(???)
    * {{{
    *   val f: Future[Int] = Future.apply(???)
    *   IO.suspendFuture(f)
    * }}}
    *
    */
  @inline def suspendInIO[T](value: => Future[T])(implicit cs: ContextShift[IO]): IO[T] =
    IOOps.suspendFuture(value)

  /**
    * !!! USE WITH CARE !!!
    *
    * Mostly here for testing. There is almost no reason whatsover for you to explicitely
    * call this in your code. You have libraries that do this for you "at the end of the world"
    * parts of your program: e.g. akka-http when waiting for the response value to a request.
    */
  @inline def unsafeSyncGet[T](value: Future[T], atMost: FiniteDuration = ConstantsAsyncEffects.defaultDuration): T =
    Await.result(value, atMost)

  //=========================================================================
  //================= Run side-effects in varrying scenarios ================
  //=========================================================================

  /**
    *
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    *
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnTrue(test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (test) FutureOps.discardContent(effect) else Future.unit

  /**
    *
    *   Runs the given effect when the value of this [[Boolean]] is ``true``
    *   Does not run the side-effect if the value is also a failed effect.
    *
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnTrue(test: Future[Boolean], effect: => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    test.flatMap(t => FutureOps.effectOnTrue(t, effect))

  /**
    *
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    *
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFalse(test: Boolean, effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (!test) FutureOps.discardContent(effect) else Future.unit

  /**
    *
    *   Runs the given effect when the value of this [[Boolean]] is ``false``
    *   Does not run the side-effect if the value is also a failed effect.
    *
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnFalse(test: Future[Boolean], effect: => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    test.flatMap(t => FutureOps.effectOnFalse(t, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[scala.None]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFail[T](value: Option[T], effect: => Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    if (value.isEmpty) FutureOps.discardContent(effect) else Future.unit

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[Option]] is [[scala.None]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnNone[T](value: Future[Option[T]], effect: => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value.flatMap(opt => FutureOps.effectOnFail(opt, effect))

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
  @inline def effectOnPure[T](value: Option[T], effect: T => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value match {
      case None    => Future.unit
      case Some(v) => FutureOps.discardContent(effect(v))

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
  @inline def flatEffectOnSome[T](value: Future[Option[T]], effect: T => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value.flatMap(opt => FutureOps.effectOnPure(opt, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnFail[T](value: Result[T], effect: Anomaly => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] = value match {
    case Correct(_)         => Future.unit
    case Incorrect(anomaly) => FutureOps.discardContent(effect(anomaly))
  }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Incorrect]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnIncorrect[T](value: Future[Result[T]], effect: Anomaly => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value.flatMap(result => FutureOps.effectOnFail(result, effect))

  /**
    *
    * @param value
    *   Runs the given effect when the value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def effectOnPure[T](value: Result[T], effect: T => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value match {
      case Incorrect(_) => Future.unit
      case Correct(v)   => FutureOps.discardContent(effect(v))
    }

  /**
    *
    * @param value
    *   Runs the given effect when the boxed value of this [[busymachines.effects.sync.Result]] is [[busymachines.effects.sync.Correct]]
    *   Does not run the side-effect if the value is also a failed effect.
    * @param effect
    *   The effect to run
    * @return
    *   Does not return anything, this method is inherently imperative, and relies on
    *   side-effects to achieve something.
    */
  @inline def flatEffectOnCorrect[T](value: Future[Result[T]], effect: T => Future[_])(
    implicit
    ec: ExecutionContext,
  ): Future[Unit] =
    value.flatMap(result => FutureOps.effectOnPure(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  /**
    * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
    * "bi" map, because it also allows you to change both branches of the effect, not just the
    * happy path.
    */
  @inline def bimap[T, R](value: Future[T], good: T => R, bad: Throwable => Anomaly)(
    implicit ec:                 ExecutionContext,
  ): Future[R] =
    value.transform(tr => tr.bimap(good, bad))

  /**
    * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
    */
  @inline def bimap[T, R](value: Future[T], result: Result[T] => Result[R])(implicit ec: ExecutionContext): Future[R] =
    FutureOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => FutureOps.pure(v)
      case Incorrect(v) => FutureOps.fail(v)
    }

  /**
    * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
    *
    * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
    */
  @inline def bimapThr[T, R](value: Future[T], good: T => R, bad: Throwable => Throwable)(
    implicit ec:                    ExecutionContext,
  ): Future[R] =
    value.transform(tr => tr.bimapThr(good, bad))

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
  @inline def morph[T, R](value: Future[T], good: T => R, bad: Throwable => R)(
    implicit ec:                 ExecutionContext,
  ): Future[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  /**
    * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
    * as the corresponding branches of a Result type.
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  @inline def morph[T, R](value: Future[T], result: Result[T] => R)(implicit ec: ExecutionContext): Future[R] =
    FutureOps.attemptResult(value).map(result)

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. computation, and side-effects captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  @inline def discardContent(value: Future[_])(implicit ec: ExecutionContext): Future[Unit] =
    value.map(ConstantsAsyncEffects.UnitFunction1)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    *
    * Similar to [[scala.concurrent.Future.traverse]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[scala.concurrent.Future.traverse]]
    */
  @inline def traverse_[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[M[A], B, M[B]],
    ec:  ExecutionContext,
  ): Future[Unit] =
    FutureOps.discardContent(Future.traverse(in)(fn))

  /**
    *
    * Similar to [[scala.concurrent.Future.sequence]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[scala.concurrent.Future.sequence]]
    */
  @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(
    implicit
    cbf: CanBuildFrom[M[Future[A]], A, M[A]],
    ec:  ExecutionContext,
  ): Future[Unit] = FutureOps.discardContent(Future.sequence(in))

  /**
    *
    * Syntactically inspired from [[Future#traverse]], but it differs semantically
    * insofar as this method does not attempt to run any futures in parallel. "M" stands
    * for "monadic", as opposed to "applicative" which is the foundation for the formal definition
    * of "traverse" (even though in Scala it is by accident-ish)
    *
    * For the vast majority of cases you should prefer this method over [[Future#sequence]]
    * and [[Future#traverse]], since even small collections can easily wind up queuing so many
    * [[Future]]s that you blow your execution context.
    *
    * Usage:
    * {{{
    *   import busymachines.effects.async._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: Future[Seq[Patch]] = Future.serialize(patches){ patch: Patch =>
    *     Future {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  @inline def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
    ec:  ExecutionContext,
  ): Future[C[B]] = {
    import scala.collection.mutable
    if (col.isEmpty) {
      Future.successful(cbf.apply().result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      val firstBuilder = fn(head).map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Future[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Future[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder.flatMap[mutable.Builder[B, C[B]]] { result: mutable.Builder[B, C[B]] =>
            val f: Future[mutable.Builder[B, C[B]]] = fn(element).map { newElement =>
              result.+=(newElement)
            }
            f
          }
      }
      eventualBuilder.map { b =>
        b.result()
      }
    }
  }

  /**
    * @see [[serialize]]
    *
    * Similar to [[serialize]], but discards all content. i.e. used only
    * for the combined effects.
    */
  @inline def serialize_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Future[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
    ec:  ExecutionContext,
  ): Future[Unit] = FutureOps.discardContent(Future.serialize(col)(fn))
}
