package busymachines.effects.async

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.sync.validated._
import cats.effect.ContextShift
import cats.{effect => ce}

import scala.collection.compat._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait IOTypeDefinitions {

  final type IO[T] = ce.IO[T]
  @inline final def IO: ce.IO.type = ce.IO
}

object IOSyntax {

  /**
    *
    */
  trait Implicits {
    implicit final def bmcIOCompanionObjectOps(obj: ce.IO.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcIOReferenceOps[T](value: IO[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcIONestedOptionOps[T](nopt: IO[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit final def bmcIONestedResultOps[T](result: IO[Result[T]]): NestedResultOps[T] =
      new NestedResultOps(result)

    implicit final def bmcIOBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit final def bmcIONestedBooleanOps(test: IO[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: ce.IO.type) extends AnyVal {

    // —— def pure[T](value: T): IO[T] —— already defined on companion object

    /**
      * Failed effect but with an [[busymachines.core.Anomaly]]
      */
    @inline def fail[T](bad: Anomaly): IO[T] =
      IOOps.fail(bad)

    /**
      * Failed effect but with a [[java.lang.Throwable]]
      */
    @inline def failThr[T](bad: Throwable): IO[T] =
      IOOps.failThr(bad)

    // —— def unit: IO[Unit] —— already defined on IO object

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.fromOption(opt, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): IO[T] =
      IOOps.suspendOption(opt, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): IO[T] =
      IOOps.fromOptionThr(opt, ifNone)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Option]].
      * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Option]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromOption]]
      */
    @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): IO[T] =
      IOOps.suspendOptionThr(opt, ifNone)

    /**
      * [[scala.util.Failure]] is sequenced into this effect
      * [[scala.util.Success]] is the pure value of this effect
      */
    @inline def fromTry[T](tr: Try[T]): IO[T] =
      IOOps.fromTry(tr)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
      * Failed Try yields a failed effect
      * Successful Try yields a pure effect
      *
      * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromTry]]
      */
    @inline def suspendTry[T](tr: => Try[T]): IO[T] =
      IOOps.suspendTry(tr)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEitherAnomaly[L, R](either: Either[L, R], transformLeft: L => Anomaly): IO[R] =
      IOOps.fromEither(either, transformLeft)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): IO[R] =
      IOOps.suspendEither(either, transformLeft)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
      * if it is a [[java.lang.Throwable]].
      */
    @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.fromEitherThr(either)(ev)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
      IOOps.suspendEitherThr(either)(ev)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): IO[R] =
      IOOps.fromEitherThr(either, transformLeft)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Either]].
      * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      *
      * N.B. this is useless if the [[Either]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromEither]]
      */
    @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): IO[R] =
      IOOps.suspendEitherThr(either, transformLeft)

    /**
      *
      * Lift the [[busymachines.effects.sync.Result]] in this effect
      * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
      * [[busymachines.effects.sync.Correct]] becomes a pure effect
      *
      */
    @inline def fromResult[T](result: Result[T]): IO[T] =
      IOOps.fromResult(result)

    /**
      * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
      * Other than that it has the semantics of [[IOOps.fromResult]]
      *
      * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromResult]]
      */
    @inline def suspendResult[T](result: => Result[T]): IO[T] =
      IOOps.suspendResult(result)

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
    @inline def fromValidated[T](value: Validated[T]): IO[T] =
      IOOps.fromValidated(value)

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
      *   IO.fromValidated(
      *     Validated.pure(42),
      *     TVFs
      *   )
      *   //in validated postfix notation it's infinitely more concise
      *   Validated.pure(42).asIO(TVFs)
      * }
      * }}}
      *
      */
    @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.fromValidated(value, ctor)

    /**
      *
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[IOOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T]): IO[T] =
      IOOps.suspendValidated(value)

    /**
      * Suspend any side-effects that might happen during the creation of this [[Validated]].
      *
      * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
      * You might as well use [[FutureOps.fromValidated]]
      */
    @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
      IOOps.suspendValidated(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * In 99% of the cases you actually want to use [[IOOps.suspendFuture]]
      *
      * If you are certain that this [[Future]] is pure, then you can use
      * this method to lift it into [[IO]].
      */
    @inline def fromFuturePure[T](future: Future[T])(implicit cs: ContextShift[IO]): IO[T] =
      IOOps.fromFuturePure(future)

    /**
      *
      * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
      * most important operation when it comes to inter-op between the two effects.
      *
      * Usage. N.B. that this only makes sense if the creation of the Future itself
      * is also suspended in the [[IO]].
      * {{{
      * @inline def writeToDB(v: Int, s: String): Future[Long] = ???
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
    @inline def suspendFuture[T](result: => Future[T])(implicit cs: ContextShift[IO]): IO[T] =
      IOOps.suspendFuture(result)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): IO[T] =
      IOOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): IO[T] =
      IOOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWith[T](test: Boolean, good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condWithThr[T](test: Boolean, good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.condWithThr(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCond[T](test: IO[Boolean], good: => T, bad: => Anomaly): IO[T] =
      IOOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondThr[T](test: IO[Boolean], good: => T, bad: => Throwable): IO[T] =
      IOOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWith[T](test: IO[Boolean], good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def flatCondWithThr[T](test: IO[Boolean], good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrue(test: Boolean, bad: => Anomaly): IO[Unit] =
      IOOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueThr(test: Boolean, bad: => Throwable): IO[Unit] =
      IOOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalse(test: Boolean, bad: => Anomaly): IO[Unit] =
      IOOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseThr(test: Boolean, bad: => Throwable): IO[Unit] =
      IOOps.failOnFalseThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrue(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def flatFailOnTrueThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalse(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def flatFailOnFalseThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnFalseThr(test, bad)

    /**
      * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOption[T](nopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
      IOOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackOptionThr[T](nopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
      IOOps.unpackOptionThr(nopt, ifNone)

    /**
      * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def unpackResult[T](value: IO[Result[T]]): IO[T] =
      IOOps.unpackResult(value)

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def attemptResult[T](value: IO[T]): IO[Result[T]] =
      IOOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * The moment you call this, the side-effects suspended in this [[IO]] start being
      * executed.
      */
    @inline def asFutureUnsafe[T](value: IO[T]): Future[T] =
      IOOps.asFutureUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet[T](value: IO[T]): T =
      IOOps.unsafeSyncGet(value)

    //=========================================================================
    //================= Run side-effects in varrying scenarios ================
    //=========================================================================

    /**
      *
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
    @inline def effectOnTrue(test: Boolean, effect: => IO[_]): IO[Unit] =
      IOOps.effectOnTrue(test, effect)

    /**
      *
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
    @inline def flatEffectOnTrue(test: IO[Boolean], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnTrue(test, effect)

    /**
      *
      *
      *   Runs the given effect when the value of this [[Boolean]] is ``false``
      *
      * @param effect
      *   The effect to run
      * @return
      *   Does not return anything, this method is inherently imperative, and relies on
      *   side-effects to achieve something.
      */
    @inline def effectOnFalse(test: Boolean, effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFalse(test, effect)

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
    @inline def flatEffectOnFalse(test: IO[Boolean], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnFalse(test, effect)

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
    @inline def effectOnFail[T](value: Option[T], effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

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
    @inline def flatEffectOnNone[T](value: IO[Option[T]], effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnNone(value, effect)

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
    @inline def effectOnPure[T](value: Option[T], effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

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
    @inline def flatEffectOnSome[T](value: IO[Option[T]], effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnSome(value, effect)

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
    @inline def effectOnFail[T](value: Result[T], effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.effectOnFail(value, effect)

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
    @inline def flatEffectOnIncorrect[T](value: IO[Result[T]], effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.flatEffectOnIncorrect(value, effect)

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
    @inline def flatEffectOnCorrect[T](value: IO[Result[T]], effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnCorrect(value, effect)

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
    @inline def effectOnPure[T](value: Result[T], effect: T => IO[_]): IO[Unit] =
      IOOps.effectOnPure(value, effect)

    //=========================================================================
    //============================== Transformers =============================
    //=========================================================================

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[T, R](value: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
      IOOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[T, R](value: IO[T], result: Result[T] => Result[R]): IO[R] =
      IOOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
      */
    @inline def bimapThr[T, R](value: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
      IOOps.bimapThr(value, good, bad)

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
    @inline def morph[T, R](value: IO[T], good: T => R, bad: Throwable => R): IO[R] =
      IOOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[T, R](value: IO[T], result: Result[T] => R): IO[R] =
      IOOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent(value: IO[_]): IO[Unit] =
      IOOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * {{{
      * @inline def indexToFilename(i: Int): IO[String] = ???
      *
      *   val fileIndex: List[Int] = List(0,1,2,3,4)
      *   val fileNames: IO[List[String]] = IO.traverse(fileIndex){ i =>
      *     indexToFilename(i)
      *   }
      * }}}
      */
    @inline def traverse[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): IO[C[B]] = IOOps.traverse(col)(fn)

    /**
      * Similar to [[traverse]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[traverse]]
      *
      */
    @inline def traverse_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): IO[Unit] = IOOps.traverse_(col)(fn)

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * Specialized case of [[traverse]]
      *
      * {{{
      * @inline def indexToFilename(i: Int): IO[String] = ???
      *
      *   val fileNamesIO: List[IO[String]] = List(0,1,2,3,4).map(indexToFileName)
      *   val fileNames: IO[List[String]] = IO.sequence(fileNamesIO)
      * }}}
      */
    @inline def sequence[A, M[X] <: IterableOnce[X]](in: M[IO[A]])(
      implicit
      cbf: BuildFrom[M[IO[A]], A, M[A]],
    ): IO[M[A]] = IOOps.sequence(in)

    /**
      * Similar to [[sequence]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[sequence]]
      *
      */
    @inline def sequence_[A, M[X] <: IterableOnce[X]](in: M[IO[A]])(
      implicit
      cbf: BuildFrom[M[IO[A]], A, M[A]],
    ): IO[Unit] = IOOps.sequence_(in)

    /**
      *
      * Syntactically inspired from [[Future#traverse]].
      *
      * See [[FutureOps.serialize]] for semantics.
      *
      * Usage:
      * {{{
      *   import busymachines.effects.async._
      *   val patches: Seq[Patch] = //...
      *
      *   //this ensures that no two changes will be applied in parallel.
      *   val allPatches: IO[Seq[Patch]] = IO.serialize(patches){ patch: Patch =>
      *     IO {
      *       //apply patch
      *     }
      *   }
      *   //... and so on, and so on!
      * }}}
      *
      *
      */
    @inline def serialize[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): IO[C[B]] = IOOps.serialize(col)(fn)

    /**
      * Similar to [[serialize]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * @see [[serialize]]
      *
      */
    @inline def serialize_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): IO[Unit] = IOOps.serialize_(col)(fn)

  }

  /**
    *
    */
  final class ReferenceOps[T](val value: IO[T]) extends AnyVal {

    /**
      * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
      *
      * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def attempResult: IO[Result[T]] =
      IOOps.attemptResult(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * The moment you call this, the side-effects suspended in this [[IO]] start being
      * executed.
      */
    @inline def asFutureUnsafe(): Future[T] =
      IOOps.asFutureUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Mostly here for testing. There is almost no reason whatsover for you to explicitely
      * call this in your code. You have libraries that do this for you "at the end of the world"
      * parts of your program: e.g. akka-http when waiting for the response value to a request.
      */
    @inline def unsafeSyncGet(): T =
      IOOps.unsafeSyncGet(value)

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    @inline def bimap[R](good: T => R, bad: Throwable => Anomaly): IO[R] =
      IOOps.bimap(value, good, bad)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      */
    @inline def bimap[R](result: Result[T] => Result[R]): IO[R] =
      IOOps.bimap(value, result)

    /**
      * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
      * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
      *
      * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
      */
    @inline def bimapThr[R](good: T => R, bad: Throwable => Throwable): IO[R] =
      IOOps.bimapThr(value, good, bad)

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
    @inline def morph[R](good: T => R, bad: Throwable => R): IO[R] =
      IOOps.morph(value, good, bad)

    /**
      * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
      * as the corresponding branches of a Result type.
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    @inline def morph[R](result: Result[T] => R): IO[R] =
      IOOps.morph(value, result)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. computation, and side-effects captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent: IO[Unit] =
      IOOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](val nopt: IO[Option[T]]) extends AnyVal {

    /**
      * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpack(ifNone: => Anomaly): IO[T] =
      IOOps.unpackOption(nopt, ifNone)

    /**
      * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    @inline def unpackThr(ifNone: => Throwable): IO[T] =
      IOOps.unpackOptionThr(nopt, ifNone)

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
    @inline def effectOnFail(effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnNone(nopt, effect)

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
    @inline def effectOnPure(effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnSome(nopt, effect)

  }

  /**
    *
    */
  final class NestedResultOps[T](val result: IO[Result[T]]) extends AnyVal {

    /**
      * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
      *
      * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
      */
    @inline def unpack: IO[T] =
      IOOps.unpackResult(result)

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
    @inline def effectOnFail(effect: Anomaly => IO[_]): IO[Unit] =
      IOOps.flatEffectOnIncorrect(result, effect)

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
    @inline def effectOnPure(effect: T => IO[_]): IO[Unit] =
      IOOps.flatEffectOnCorrect(result, effect)
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
    @inline def condIO[T](good: => T, bad: => Anomaly): IO[T] =
      IOOps.cond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condIOThr[T](good: => T, bad: => Throwable): IO[T] =
      IOOps.condThr(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWithIO[T](good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.condWith(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      */
    @inline def condWithIOThr[T](good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.condWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueIO(bad: => Anomaly): IO[Unit] =
      IOOps.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def failOnTrueIOThr(bad: => Throwable): IO[Unit] =
      IOOps.failOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseIO(bad: => Anomaly): IO[Unit] =
      IOOps.failOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def failOnFalseIOThr(bad: => Throwable): IO[Unit] =
      IOOps.failOnFalseThr(test, bad)

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
    @inline def effectOnFalseIO(effect: => IO[_]): IO[Unit] =
      IOOps.effectOnFalse(test, effect)

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
    @inline def effectOnTrueIO(effect: => IO[_]): IO[Unit] =
      IOOps.effectOnTrue(test, effect)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(val test: IO[Boolean]) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def cond[T](good: => T, bad: => Anomaly): IO[T] =
      IOOps.flatCond(test, good, bad)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condThr[T](good: => T, bad: => Throwable): IO[T] =
      IOOps.flatCondThr(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWith[T](good: => IO[T], bad: => Anomaly): IO[T] =
      IOOps.flatCondWith(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    @inline def condWithThr[T](good: => IO[T], bad: => Throwable): IO[T] =
      IOOps.flatCondWithThr(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrue(bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    @inline def failOnTrueThr(bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnTrueThr(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def failOnFalse(bad: => Anomaly): IO[Unit] =
      IOOps.flatFailOnFalse(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    @inline def failOnFalseThr(bad: => Throwable): IO[Unit] =
      IOOps.flatFailOnFalseThr(test, bad)

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
    @inline def effectOnFalse(effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnFalse(test, effect)

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
    @inline def effectOnTrue(effect: => IO[_]): IO[Unit] =
      IOOps.flatEffectOnTrue(test, effect)

  }
}

/**
  *
  */
object IOOps {
  import cats.syntax.applicativeError._
  import cats.syntax.monadError._

  /**
    * Failed effect but with an [[busymachines.core.Anomaly]]
    */
  @inline def fail[T](bad: Anomaly): IO[T] =
    IO.raiseError(bad.asThrowable)

  /**
    * Failed effect but with a [[java.lang.Throwable]]
    */
  @inline def failThr[T](bad: Throwable): IO[T] =
    IO.raiseError(bad)

  // —— def unit: IO[Unit] —— already defined on IO object

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): IO[T] = opt match {
    case None        => IOOps.fail(ifNone)
    case Some(value) => IO.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[scala.None]] then we get back a failed effect with the given [[busymachines.core.Anomaly]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromOption]]
    */
  @inline def suspendOption[T](opt: => Option[T], ifNone: => Anomaly): IO[T] =
    IO.suspend(IOOps.fromOption(opt, ifNone))

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def fromOptionThr[T](opt: Option[T], ifNone: => Throwable): IO[T] = opt match {
    case None        => IOOps.failThr(ifNone)
    case Some(value) => IO.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Option]].
    * If the option is [[scala.None]] then we get back a failed effect with the given [[java.lang.Throwable]]
    *
    * N.B. this is useless if the [[Option]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromOption]]
    */
  @inline def suspendOptionThr[T](opt: => Option[T], ifNone: => Throwable): IO[T] =
    IO.suspend(IOOps.fromOptionThr(opt, ifNone))

  /**
    * [[scala.util.Failure]] is sequenced into this effect
    * [[scala.util.Success]] is the pure value of this effect
    */
  @inline def fromTry[T](tr: Try[T]): IO[T] = tr match {
    case scala.util.Success(v) => IO.pure(v)
    case scala.util.Failure(t) => IO.raiseError(t)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[scala.util.Try]].
    * Failed Try yields a failed effect
    * Successful Try yields a pure effect
    *
    * N.B. this is useless if the [[scala.util.Try]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromTry]]
    */
  @inline def suspendTry[T](tr: => Try[T]): IO[T] =
    IO.suspend(IOOps.fromTry(tr))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEither[L, R](either: Either[L, R], transformLeft: L => Anomaly): IO[R] = either match {
    case Left(value)  => IOOps.fail(transformLeft(value))
    case Right(value) => IO.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[busymachines.core.Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromEither]]
    */
  @inline def suspendEither[L, R](either: => Either[L, R], transformLeft: L => Anomaly): IO[R] =
    IO.suspend(IOOps.fromEither(either, transformLeft))

  /**
    * Lift this [[Either]] and  sequence its left-hand-side [[java.lang.Throwable]] within this effect
    * if it is a [[java.lang.Throwable]].
    */
  @inline def fromEitherThr[L, R](either: Either[L, R])(implicit ev: L <:< Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(ev(value))
    case Right(value) => IO.pure(value)
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And sequence its left-hand-side [[java.lang.Throwable]] within this effect if it is a [[java.lang.Throwable]]
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](either: => Either[L, R])(implicit ev: L <:< Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either)(ev))

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    */
  @inline def fromEitherThr[L, R](either: Either[L, R], transformLeft: L => Throwable): IO[R] = either match {
    case Left(value)  => IOOps.failThr(transformLeft(value))
    case Right(value) => IO.pure(value)
  }

  /**
    * Suspend any side-effects that might happen during the creation of this [[Either]].
    * And transform its left-hand side into a [[java.lang.Throwable]] and sequence it within
    * this effect, yielding a failed effect.
    *
    * N.B. this is useless if the [[Either]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromEither]]
    */
  @inline def suspendEitherThr[L, R](either: => Either[L, R], transformLeft: L => Throwable): IO[R] =
    IO.suspend(IOOps.fromEitherThr(either, transformLeft))

  /**
    *
    * Lift the [[busymachines.effects.sync.Result]] in this effect
    * [[busymachines.effects.sync.Incorrect]] becomes a failed effect
    * [[busymachines.effects.sync.Correct]] becomes a pure effect
    *
    */
  @inline def fromResult[T](result: Result[T]): IO[T] = result match {
    case Left(value)  => IOOps.fail(value)
    case Right(value) => IO.pure(value)
  }

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
  @inline def fromValidated[T](value: Validated[T]): IO[T] = value match {
    case cats.data.Validated.Valid(e)   => IO.pure(e)
    case cats.data.Validated.Invalid(e) => IOOps.fail(GenericValidationFailures(e.head, e.tail))
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
    *   IO.fromValidated(
    *     Validated.pure(42),
    *     TVFs
    *   )
    *   //in validated postfix notation it's infinitely more concise
    *   Validated.pure(42).asIO(TVFs)
    * }
    * }}}
    *
    */
  @inline def fromValidated[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] = value match {
    case cats.data.Validated.Valid(e)   => IO.pure(e)
    case cats.data.Validated.Invalid(e) => IOOps.fail(ctor(e.head, e.tail))
  }

  /**
    *
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T]): IO[T] =
    IO.suspend(IOOps.fromValidated(value))

  /**
    * Suspend any side-effects that might happen during the creation of this [[Validated]].
    *
    * N.B. this is useless if the [[Validated]] was previously assigned to a "val".
    * You might as well use [[FutureOps.fromValidated]]
    */
  @inline def suspendValidated[T](value: => Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): IO[T] =
    IO.suspend(IOOps.fromValidated(value, ctor))

  /**
    * Suspend any side-effects that might happen during the creation of this [[busymachines.effects.sync.Result]].
    * Other than that it has the semantics of [[IOOps.fromResult]]
    *
    * N.B. this is useless if the [[busymachines.effects.sync.Result]] was previously assigned to a "val".
    * You might as well use [[IOOps.fromResult]]
    */
  @inline def suspendResult[T](result: => Result[T]): IO[T] =
    IO.suspend(IOOps.fromResult(result))

  /**
    * !!! USE WITH CARE !!!
    *
    * In 99% of the cases you actually want to use [[IOOps.suspendFuture]]
    *
    * If you are certain that this [[Future]] is pure, then you can use
    * this method to lift it into [[IO]].
    */
  @inline def fromFuturePure[T](value: Future[T])(implicit cs: ContextShift[IO]): IO[T] =
    IO.fromFuture(IO(value))

  /**
    *
    * Suspend the side-effects of this [[Future]] into an [[IO]]. This is the
    * most important operation when it comes to inter-op between the two effects.
    *
    * Usage. N.B. that this only makes sense if the creation of the Future itself
    * is also suspended in the [[IO]].
    * {{{
    * @inline def writeToDB(v: Int, s: String): Future[Long] = ???
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
  @inline def suspendFuture[T](value: => Future[T])(implicit cs: ContextShift[IO]): IO[T] =
    IO.fromFuture(IO(value)).guarantee(cs.shift)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): IO[T] =
    if (test) IO.pure(good) else IOOps.fail(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    */
  @inline def condThr[T](test: Boolean, good: => T, bad: => Throwable): IO[T] =
    if (test) IO.pure(good) else IOOps.failThr(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def condWith[T](test: Boolean, good: => IO[T], bad: => Anomaly): IO[T] =
    if (test) good else IOOps.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    */
  @inline def condWithThr[T](test: Boolean, good: => IO[T], bad: => Throwable): IO[T] =
    if (test) good else IOOps.failThr(bad)

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCond[T](test: IO[Boolean], good: => T, bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.cond(t, good, bad))

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondThr[T](test: IO[Boolean], good: => T, bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condThr(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWith[T](test: IO[Boolean], good: => IO[T], bad: => Anomaly): IO[T] =
    test.flatMap(t => IOOps.condWith(t, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[java.lang.Throwable]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  @inline def flatCondWithThr[T](test: IO[Boolean], good: => IO[T], bad: => Throwable): IO[T] =
    test.flatMap(t => IOOps.condWithThr(t, good, bad))

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrue(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (test) IOOps.fail(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def failOnTrueThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (test) IOOps.failThr(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalse(test: Boolean, bad: => Anomaly): IO[Unit] =
    if (!test) IOOps.fail(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def failOnFalseThr(test: Boolean, bad: => Throwable): IO[Unit] =
    if (!test) IOOps.failThr(bad) else IO.unit

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrue(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrue(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  @inline def flatFailOnTrueThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnTrueThr(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalse(test: IO[Boolean], bad: => Anomaly): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalse(t, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  @inline def flatFailOnFalseThr(test: IO[Boolean], bad: => Throwable): IO[Unit] =
    test.flatMap(t => IOOps.failOnFalseThr(t, bad))

  /**
    * Sequences the given [[busymachines.core.Anomaly]] if Option is [[scala.None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOption[T](nopt: IO[Option[T]], ifNone: => Anomaly): IO[T] =
    nopt.flatMap {
      case None    => IOOps.fail(ifNone)
      case Some(v) => IO.pure(v)
    }

  /**
    * Sequences the given [[java.lang.Throwable]] if Option is [[scala.None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  @inline def unpackOptionThr[T](nopt: IO[Option[T]], ifNone: => Throwable): IO[T] =
    nopt.flatMap {
      case None    => IOOps.failThr(ifNone)
      case Some(v) => IO.pure(v)
    }

  /**
    * Sequences the failure of the [[busymachines.effects.sync.Incorrect]] [[busymachines.effects.sync.Result]] into this effect.
    *
    * The failure of this effect takes precedence over the failure of the [[busymachines.effects.sync.Incorrect]] value.
    */
  @inline def unpackResult[T](value: IO[Result[T]]): IO[T] = value.flatMap {
    case Left(a)  => IOOps.fail(a)
    case Right(a) => IO.pure(a)
  }

  /**
    * Makes the failure, and non-failure part of this effect explicit in a [[busymachines.effects.sync.Result]] type.
    *
    * This transforms any failed effect, into a pure one with and [[busymachines.effects.sync.Incorrect]] value.
    */
  @inline def attemptResult[T](value: IO[T]): IO[Result[T]] =
    value.attempt.map((e: Either[Throwable, T]) => Result.fromEitherThr(e))

  /**
    * !!! USE WITH CARE !!!
    *
    * The moment you call this, the side-effects suspended in this [[IO]] start being
    * executed.
    */
  @inline def asFutureUnsafe[T](value: IO[T]): Future[T] =
    value.unsafeToFuture()

  /**
    * !!! USE WITH CARE !!!
    *
    * Mostly here for testing. There is almost no reason whatsover for you to explicitely
    * call this in your code. You have libraries that do this for you "at the end of the world"
    * parts of your program: e.g. akka-http when waiting for the response value to a request.
    */
  @inline def unsafeSyncGet[T](value: IO[T]): T =
    value.unsafeRunSync()

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
  @inline def effectOnTrue(test: Boolean, effect: => IO[_]): IO[Unit] =
    if (test) IOOps.discardContent(effect) else IO.unit

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
  @inline def flatEffectOnTrue(test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnTrue(t, effect))

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
  @inline def effectOnFalse(test: Boolean, effect: => IO[_]): IO[Unit] =
    if (!test) IOOps.discardContent(effect) else IO.unit

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
  @inline def flatEffectOnFalse(test: IO[Boolean], effect: => IO[_]): IO[Unit] =
    test.flatMap(t => IOOps.effectOnFalse(t, effect))

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
  @inline def effectOnFail[T](value: Option[T], effect: => IO[_]): IO[Unit] =
    if (value.isEmpty) IOOps.discardContent(effect) else IO.unit

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
  @inline def flatEffectOnNone[T](value: IO[Option[T]], effect: => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnFail(opt, effect))

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
  @inline def effectOnPure[T](value: Option[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case None    => IO.unit
      case Some(v) => IOOps.discardContent(effect(v))

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
  @inline def flatEffectOnSome[T](value: IO[Option[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(opt => IOOps.effectOnPure(opt, effect))

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
  @inline def effectOnFail[T](value: Result[T], effect: Anomaly => IO[_]): IO[Unit] = value match {
    case Correct(_)         => IO.unit
    case Incorrect(anomaly) => IOOps.discardContent(effect(anomaly))
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
  @inline def flatEffectOnIncorrect[T](value: IO[Result[T]], effect: Anomaly => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnFail(result, effect))

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
  @inline def effectOnPure[T](value: Result[T], effect: T => IO[_]): IO[Unit] =
    value match {
      case Incorrect(_) => IO.unit
      case Correct(v)   => IOOps.discardContent(effect(v))
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
  @inline def flatEffectOnCorrect[T](value: IO[Result[T]], effect: T => IO[_]): IO[Unit] =
    value.flatMap(result => IOOps.effectOnPure(result, effect))

  //=========================================================================
  //============================== Transformers =============================
  //=========================================================================

  /**
    * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
    * "bi" map, because it also allows you to change both branches of the effect, not just the
    * happy path.
    */
  @inline def bimap[T, R](value: IO[T], good: T => R, bad: Throwable => Anomaly): IO[R] =
    value.map(good).adaptError {
      case NonFatal(t) => bad(t).asThrowable
    }

  /**
    * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
    */
  @inline def bimap[T, R](value: IO[T], result: Result[T] => Result[R]): IO[R] =
    IOOps.attemptResult(value).map(result).flatMap {
      case Correct(v)   => IO.pure(v)
      case Incorrect(v) => IOOps.fail(v)
    }

  /**
    * Similar to the overload, but the [[busymachines.effects.sync.Correct]] branch of the result is used to change the "pure" branch of this
    * effect, and [[busymachines.effects.sync.Incorrect]] branch is used to change the "fail" branch of the effect.
    *
    * The overload that uses [[java.lang.Throwable]] instead of [[busymachines.core.Anomaly]]
    */
  @inline def bimapThr[T, R](value: IO[T], good: T => R, bad: Throwable => Throwable): IO[R] =
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
  @inline def morph[T, R](value: IO[T], good: T => R, bad: Throwable => R): IO[R] =
    value.map(good).recover {
      case NonFatal(t) => bad(t)
    }

  /**
    * Semantically equivalent to the overload ``morph`` that accepts two functions, but those encoded
    * as the corresponding branches of a Result type.
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  @inline def morph[T, R](value: IO[T], result: Result[T] => R): IO[R] =
    IOOps.attemptResult(value).map(result)

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. computation, and side-effects captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  @inline def discardContent(value: IO[_]): IO[Unit] =
    value.map(ConstantsAsyncEffects.UnitFunction1)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    * @inline def indexToFilename(i: Int): IO[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: IO[List[String]] = IO.traverse(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
    */
  @inline def traverse[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): IO[C[B]] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    import scala.collection.mutable

    if (col.iterator.isEmpty) {
      IO.pure(cbf.newBuilder(col).result())
    }
    else {
      //OK, super inneficient, need a better implementation
      val result:  IO[List[B]]              = col.iterator.toList.traverse(fn)
      val builder: mutable.Builder[B, C[B]] = cbf.newBuilder(col)
      result.map(_.foreach(e => builder.+=(e))).map(_ => builder.result())
    }
  }

  /**
    * Similar to [[traverse]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[traverse]]
    *
    */
  @inline def traverse_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): IO[Unit] = IOOps.discardContent(IOOps.traverse(col)(fn))

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    * @inline def indexToFilename(i: Int): IO[String] = ???
    *
    *   val fileNamesIO: List[IO[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames: IO[List[String]] = IO.sequence(fileNamesIO)
    * }}}
    */
  @inline def sequence[A, M[X] <: IterableOnce[X]](in: M[IO[A]])(
    implicit
    cbf: BuildFrom[M[IO[A]], A, M[A]],
  ): IO[M[A]] = IOOps.traverse(in)(identity)

  /**
    * Similar to [[sequence]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[sequence]]
    *
    */
  @inline def sequence_[A, M[X] <: IterableOnce[X]](in: M[IO[A]])(
    implicit
    cbf: BuildFrom[M[IO[A]], A, M[A]],
  ): IO[Unit] = IOOps.discardContent(IOOps.sequence(in))

  /**
    *
    * Syntactically inspired from [[Future#traverse]].
    *
    * See [[FutureOps.serialize]] for semantics.
    *
    * Usage:
    * {{{
    *   import busymachines.effects.async._
    *   val patches: Seq[Patch] = //...
    *
    *   //this ensures that no two changes will be applied in parallel.
    *   val allPatches: IO[Seq[Patch]] = IO.serialize(patches){ patch: Patch =>
    *     IO {
    *       //apply patch
    *     }
    *   }
    *   //... and so on, and so on!
    * }}}
    *
    *
    */
  @inline def serialize[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): IO[C[B]] = IOOps.traverse(col)(fn)(cbf)

  /**
    * Similar to [[serialize]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * @see [[serialize]]
    *
    */
  @inline def serialize_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => IO[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): IO[Unit] = IOOps.discardContent(IOOps.serialize(col)(fn))
}
