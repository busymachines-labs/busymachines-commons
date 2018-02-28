/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.effects.sync

import busymachines.core._

import scala.collection.generic.CanBuildFrom
import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
trait ResultTypeDefinitions {
  final type Result[T]    = Either[Anomaly, T]
  final type Correct[T]   = Right[Anomaly,  T]
  final type Incorrect[T] = Left[Anomaly,   T]
}

trait ResultCompanionAliases {
  @inline final def Result:    busymachines.effects.sync.Result.type    = busymachines.effects.sync.Result
  @inline final def Correct:   busymachines.effects.sync.Correct.type   = busymachines.effects.sync.Correct
  @inline final def Incorrect: busymachines.effects.sync.Incorrect.type = busymachines.effects.sync.Incorrect
}

object ResultSyntax {

  /**
    *
    */
  trait Implicits {

    implicit final def bmcResultReferenceOps[T](value: Result[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcResultNestedOptionOps[T](nopt: Result[Option[T]]): NestedOptionOps[T] =
      new NestedOptionOps(nopt)

    implicit final def bmcResultBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit final def bmcResultNestedBooleanOps(test: Result[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)

  }

  // —— final class CompanionObjectOps(val obj: Result.type) —— is not necessary, because we reference the Result
  // object directly in this case. A clever slight of hand

  /**
    *
    */
  final class ReferenceOps[T](val value: Result[T]) extends AnyVal {

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */

    def asOptionUnsafe(): Option[T] =
      Result.asOptionUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */

    def asListUnsafe(): List[T] =
      Result.asListUnsafe(value)

    /**
      * Transforms this result into a [[Try]]. The [[Anomaly]] on the left
      * hand side is converted into a [[Throwable]] and corresponds to a
      * failed [[Try]]
      */

    def asTry: Try[T] =
      Result.asTry(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */

    def unsafeGet(): T =
      Result.unsafeGet(value)

    /**
      * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
      * "bi" map, because it also allows you to change both branches of the effect, not just the
      * happy path.
      */
    def bimap[R](good: T => R, bad: Anomaly => Anomaly): Result[R] =
      Result.bimap(value, good, bad)

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
      *   val r: Result[Int] = Result.fail(InvalidInputFailure)
      *   Result.morph(r, (i: Int) => i *2, (t: Throwable) => 42)
      * }}}
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    def morph[R](good: T => R, bad: Anomaly => R): Result[R] =
      Result.morph(value, good, bad)

    /**
      *
      * If this effect is [[Incorrect]] then it tries to transform it into a [[Correct]] one using
      * the given function
      */
    def recover[R >: T](pf: PartialFunction[Anomaly, R]): Result[R] =
      Result.recover(value, pf)

    /**
      *
      * If this effect is [[Incorrect]] then it brings the final effect into the state returned by the
      * ``pf`` function.
      */
    def recoverWith[R >: T](pf: PartialFunction[Anomaly, Result[R]]): Result[R] =
      Result.recoverWith(value, pf)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. thecomputation captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */

    def discardContent: Result[Unit] =
      Result.discardContent(value)
  }

  /**
    *
    *
    */
  final class NestedOptionOps[T](val nopt: Result[Option[T]]) extends AnyVal {

    /**
      * Sequences the given [[Anomaly]] if Option is [[None]] into this effect
      *
      * The failure of this effect takes precedence over the given failure
      */
    def unpack(ifNone: => Anomaly): Result[T] =
      Result.unpackOption(nopt, ifNone)
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
    def condResult[T](good: => T, bad: => Anomaly): Result[T] =
      Result.cond(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      */
    def condWithResult[T](good: => Result[T], bad: => Anomaly): Result[T] =
      Result.condWith(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    def failOnTrueResult(bad: => Anomaly): Result[Unit] =
      Result.failOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    def failOnFalseResult(bad: => Anomaly): Result[Unit] =
      Result.failOnFalse(test, bad)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(val test: Result[Boolean]) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    def cond[T](good: => T, bad: => Anomaly): Result[T] =
      Result.flatCond(test, good, bad)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[Anomaly]] if boolean is false
      *   failed effect if the effect wrapping the boolean is already failed
      */
    def condWith[T](good: => Result[T], bad: => Anomaly): Result[T] =
      Result.flatCondWith(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is true, or if the original effect is failed
      */
    def failOnTrue(bad: => Anomaly): Result[Unit] =
      Result.flatFailOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boxed boolean is false, or if the original effect is failed
      */
    def failOnFalse(bad: => Anomaly): Result[Unit] =
      Result.flatFailOnFalse(test, bad)

  }
}

//=============================================================================
//=============================================================================
//=============================================================================
//=============================================================================

/**
  *
  * And alternative to this would be to alias the Either.type in [[ResultTypeDefinitions]]:
  * {{{
  *   val Result: Either.type = Either
  * }}}
  *
  * The problem with this is that it introduces conflcits when importing:
  * {{{
  *   import cats._, cats.implicits._
  * }}}
  *
  * Because of ambiguities in the same method names, and signatures which
  * only differ in that they use [[Anomaly]] instead of [[Throwable]],
  * and that is too ambiguous to the implicit search, for some reason...
  *
  * Moral of the story:
  * Overloading and implicit resolution DO NOT play well together.
  */
object Result {

  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  /**
    * N.B. pass only pure values. Otherwise use [[Result.apply]]
    */

  def pure[T](t: T): Result[T] =
    Correct(t)

  /**
    * Failed effect but with an [[Anomaly]]
    */

  def fail[T](bad: Anomaly): Result[T] =
    Incorrect(bad)

  /**
    * Failed effect but with a [[Throwable]]. Wraps in a [[CatastrophicError]]
    * if it is not also an [[Anomaly]]
    */

  def failThr[T](bad: Throwable): Result[T] = bad match {
    case a: Anomaly => Result.fail(a)
    case NonFatal(t) => Result.fail(CatastrophicError(t))
  }

  /**
    * N.B. pass only pure values. Otherwise use [[Result.apply]]
    */

  def correct[T](t: T): Result[T] =
    Correct(t)

  /**
    * Failed effect but with an [[Anomaly]]
    */

  def incorrect[T](bad: Anomaly): Result[T] =
    Incorrect(bad)

  /**
    * Failed effect but with a [[Throwable]]. Wraps in a [[CatastrophicError]]
    * if it is not also an [[Anomaly]]
    */

  def incorrectThr[T](bad: Throwable): Result[T] = bad match {
    case a: Anomaly => Result.fail(a)
    case NonFatal(t) => Result.fail(CatastrophicError(t))
  }

  val unit: Result[Unit] =
    Correct(())

  /**
    * Captures any throwed [[Throwable]], and wraps it in a [[Result]]. If it is
    * also a [[Anomaly]] then keep it as is, but if it is not then wrap it into
    * a [[CatastrophicError]].
    */
  def apply[T](thunk: => T): Result[T] = {
    try {
      Result.pure(thunk)
    } catch {
      case an: Anomaly => Result.incorrect(an)
      case NonFatal(t) => Result.incorrect(CatastrophicError(t))
    }
  }

  //===========================================================================
  //==================== Result from various other effects ====================
  //===========================================================================

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def fromOption[T](opt: Option[T], ifNone: => Anomaly): Result[T] = opt match {
    case None    => Result.incorrect(ifNone)
    case Some(v) => Result.pure(v)
  }

  /**
    * Lift this [[Try]] and  sequence its failure case [[Throwable]] within this effect.
    * If the [[Throwable]] is also an [[Anomaly]] then it is used as is for the [[Incorrect]] case,
    * but if it is not, then it is wrapped inside of a [[CatastrophicError]] anomaly.
    */
  def fromTry[T](t: Try[T]): Result[T] = t match {
    case Failure(a: Anomaly) => Result.incorrect(a)
    case Failure(NonFatal(r)) => Result.incorrect(CatastrophicError(r))
    case Success(value)       => Result.pure(value)
  }

  /**
    * Lift this [[Either]] and transform its left-hand side into a [[Anomaly]] and sequence it within
    * this effect, yielding a failed effect.
    */
  def fromEither[L, R](elr: Either[L, R], transformLeft: L => Anomaly): Result[R] = elr match {
    case Left(left)   => Result.incorrect(transformLeft(left))
    case Right(value) => Result.pure(value)
  }

  /**
    * Lift this [[Either]] and  sequence its left-hand-side [[Throwable]] within this effect
    * if it is a [[Throwable]]. If the [[Throwable]] is also an [[Anomaly]] then it is
    * used as is for the [[Incorrect]] case, but if it is not, then it is wrapped inside
    * of a [[CatastrophicError]] anomaly.
    */
  def fromEitherThr[L, R](elr: Either[L, R])(implicit ev: L <:< Throwable): Result[R] = elr match {
    case Left(left) =>
      ev(left) match {
        case a: Anomaly => Result.incorrect(a)
        case NonFatal(t) => Result.incorrect(CatastrophicError(t))
      }
    case Right(value) => Result.pure(value)
  }

  //===========================================================================
  //==================== Result from special cased Result =====================
  //===========================================================================

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  def cond[T](test: Boolean, good: => T, bad: => Anomaly): Result[T] =
    if (test) Result.pure(good) else Result.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    */
  def condWith[T](test: Boolean, good: => Result[T], bad: => Anomaly): Result[T] =
    if (test) good else Result.fail(bad)

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  def failOnTrue(test: Boolean, bad: => Anomaly): Result[Unit] =
    if (test) Result.fail(bad) else Result.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  def failOnFalse(test: Boolean, bad: => Anomaly): Result[Unit] =
    if (!test) Result.fail(bad) else Result.unit

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCond[T](test: Result[Boolean], good: => T, bad: => Anomaly): Result[T] =
    test.flatMap(b => Result.cond(b, good, bad))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[Anomaly]] if boolean is false
    *   failed effect if the effect wrapping the boolean is already failed
    */
  def flatCondWith[T](test: Result[Boolean], good: => Result[T], bad: => Anomaly): Result[T] =
    test.flatMap(b => Result.condWith(b, good, bad))

  /**
    * @return
    *   Failed effect, if the boxed boolean is true, or if the original effect is failed
    */
  def flatFailOnTrue(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
    test.flatMap(b => if (b) Result.fail(bad) else Result.unit)

  /**
    * @return
    *   Failed effect, if the boxed boolean is false, or if the original effect is failed
    */
  def flatFailOnFalse(test: Result[Boolean], bad: => Anomaly): Result[Unit] =
    test.flatMap(b => if (!b) Result.fail(bad) else Result.unit)

  /**
    * Sequences the given [[Anomaly]] if Option is [[None]] into this effect
    *
    * The failure of this effect takes precedence over the given failure
    */
  def unpackOption[T](nopt: Result[Option[T]], ifNone: => Anomaly): Result[T] =
    nopt.flatMap(opt => Result.fromOption(opt, ifNone))

  //===========================================================================
  //======================= Result to various effects =========================
  //===========================================================================

  /**
    * !!! USE WITH CARE !!!
    *
    * Throws exceptions into your face
    *
    */
  def asOptionUnsafe[T](value: Result[T]): Option[T] = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => Option(value)
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Throws exceptions into your face
    *
    */
  def asListUnsafe[T](value: Result[T]): List[T] = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => List(value)
  }

  /**
    * Transforms this result into a [[Try]]. The [[Anomaly]] on the left
    * hand side is converted into a [[Throwable]] and corresponds to a
    * failed [[Try]]
    */
  def asTry[T](value: Result[T]): Try[T] = value match {
    case Left(value)  => scala.util.Failure(value.asThrowable)
    case Right(value) => scala.util.Success(value)
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Will throw exceptions in your face if the underlying effect is failed
    */
  def unsafeGet[T](value: Result[T]): T = value match {
    case Left(value)  => throw value.asThrowable
    case Right(value) => value
  }

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  /**
    * Used to transform both the "pure" part of the effect, and the "fail" part. Hence the name
    * "bi" map, because it also allows you to change both branches of the effect, not just the
    * happy path.
    */
  def bimap[T, R](value: Result[T], good: T => R, bad: Anomaly => Anomaly): Result[R] =
    value.right.map(good).left.map(bad)

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
    *   val r: Result[Int] = Result.fail(InvalidInputFailure)
    *   Result.morph(r, (i: Int) => i *2, (t: Throwable) => 42)
    * }}}
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  def morph[T, R](value: Result[T], good: T => R, bad: Anomaly => R): Result[R] = value match {
    case Left(value)  => Result.pure(bad(value))
    case Right(value) => Result.pure(good(value))
  }

  /**
    *
    * If this effect is [[Incorrect]] then it tries to transform it into a [[Correct]] one using
    * the given function
    */
  def recover[T, R >: T](value: Result[T], pf: PartialFunction[Anomaly, R]): Result[R] = value match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => Result.pure(pf(a))
    case _ => value
  }

  /**
    *
    * If this effect is [[Incorrect]] then it brings the final effect into the state returned by the
    * ``pf`` function.
    */
  def recoverWith[T, R >: T](value: Result[T], pf: PartialFunction[Anomaly, Result[R]]): Result[R] = value match {
    case Left(a: Anomaly) if pf.isDefinedAt(a) => pf(a)
    case _ => value
  }

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. thecomputation captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  def discardContent[T](value: Result[T]): Result[Unit] =
    value.map(UnitFunction)

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    *   def indexToFilename(i: Int): Result[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: Result[List[String]] = Result.traverse(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
    */
  def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Result[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): Result[C[B]] = {
    import scala.collection.mutable
    if (col.isEmpty) {
      Result.pure(cbf.apply().result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      val firstBuilder = fn(head) map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Result[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Result[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder flatMap [Anomaly, mutable.Builder[B, C[B]]] { (result: mutable.Builder[B, C[B]]) =>
            val f: Result[mutable.Builder[B, C[B]]] = fn(element) map { newElement =>
              result.+=(newElement)
            }
            f
          }
      }
      eventualBuilder map { b =>
        b.result()
      }
    }
  }

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    *   def indexToFilename(i: Int): Result[String] = ???
    *
    *   val fileNamesResult: List[Result[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames:       Result[List[String]] = Result.sequence(fileNamesTry)
    * }}}
    */
  def sequence[A, M[X] <: TraversableOnce[X]](in: M[Result[A]])(
    implicit
    cbf: CanBuildFrom[M[Result[A]], A, M[A]]
  ): Result[M[A]] = Result.traverse(in)(identity)

  //=========================================================================
  //=============================== Constants ===============================
  //=========================================================================

  private val UnitFunction: Any => Unit = _ => ()

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

  def apply[T](value: T): Result[T] =
    Right[Anomaly, T](value)

  def unapply[A <: Anomaly, C](arg: Right[A, C]): Option[C] =
    Right.unapply(arg)
}

object Incorrect {

  def apply[T](bad: Anomaly): Result[T] =
    Left[Anomaly, T](bad)

  def unapply[A <: Anomaly, C](arg: Left[A, C]): Option[A] =
    Left.unapply(arg)
}
