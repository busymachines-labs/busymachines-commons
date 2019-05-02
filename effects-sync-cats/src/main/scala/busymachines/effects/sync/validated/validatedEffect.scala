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
package busymachines.effects.sync.validated

import busymachines.core._
import busymachines.effects.sync._
import cats.{data => cd}

import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
trait ValidatedTypeDefinitions {
  final type Validated[T] = cd.Validated[cd.NonEmptyList[Anomaly], T]

  @inline final def Validated: cd.Validated.type = cd.Validated
  //these ought to be used in pattern matches, and only minimally anyway
  @inline final val ValidE:   cd.Validated.Valid.type   = cd.Validated.Valid
  @inline final val InvalidE: cd.Validated.Invalid.type = cd.Validated.Invalid
}

object ValidatedSyntax {

  trait Implicits {
    implicit final def bmcValidatedReferenceOps[T](ops: Validated[T]): ReferenceOps[T] =
      new ReferenceOps(ops)

    implicit final def bmcValidatedCompanionObjectOps(obj: cd.Validated.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcValidatedBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)
  }

  //not implemented
  final class ReferenceOps[T](val value: Validated[T]) extends AnyVal {

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */
    @inline def asOptionUnsafe(): Option[T] =
      ValidatedOps.asOptionUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
      *
      * Throws exceptions into your face
      *
      */
    @inline def asOptionUnsafe(ctor: (Anomaly, List[Anomaly]) => Anomalies): Option[T] =
      ValidatedOps.asOptionUnsafe(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */
    @inline def asListUnsafe(): List[T] =
      ValidatedOps.asListUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
      *
      */
    @inline def asListUnsafe(ctor: (Anomaly, List[Anomaly]) => Anomalies): List[T] =
      ValidatedOps.asListUnsafe(value, ctor)

    /**
      * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]] on the left
      * hand side is converted into a [[java.lang.Throwable]] and corresponds to a
      * failed [[scala.util.Try]]
      */
    @inline def asTry: Try[T] =
      ValidatedOps.asTry(value)

    /**
      * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]]s are
      * transformed into an [[busymachines.core.Anomalies]] of your choice
      */
    @inline def asTry(ctor: (Anomaly, List[Anomaly]) => Anomalies): Try[T] =
      ValidatedOps.asTry(value, ctor)

    @inline def asResult: Result[T] =
      ValidatedOps.asResult(value)

    @inline def asResult(ctor: (Anomaly, List[Anomaly]) => Anomalies): Result[T] =
      ValidatedOps.asResult(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    @inline def unsafeGet(): T =
      ValidatedOps.unsafeGet(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions of your choice in your face if the underlying effect is failed
      */
    @inline def unsafeGet(ctor: (Anomaly, List[Anomaly]) => Anomalies): T =
      ValidatedOps.unsafeGet(value, ctor)

    @inline def discardContent: Validated[Unit] =
      ValidatedOps.discardContent(value)

  }

  //not implemented
  //final class NestedOptionOps[T](val nopt: Validated[Option[T]]) extends AnyVal

  final class BooleanOps(val test: Boolean) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def cond[T](good: => T, bad: => Anomaly): Validated[T] =
      ValidatedOps.cond(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWith[T](good: => Validated[T], bad: => Anomaly): Validated[T] =
      ValidatedOps.condWith(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def invalidOnTrue(bad: => Anomaly): Validated[Unit] =
      ValidatedOps.invalidOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def invalidOnFalse(bad: => Anomaly): Validated[Unit] =
      ValidatedOps.invalidOnFalse(test, bad)

  }

  final class CompanionObjectOps(val obj: cd.Validated.type) {

    /**
      * N.B. pass only pure values.
      */
    @inline def pure[T](value: T): Validated[T] = ValidatedOps.pure(value)

    /**
      * Failed effect
      */
    @inline def fail[T](bad: Anomaly, bads: Anomaly*): Validated[T] = ValidatedOps.fail(bad, bads: _*)

    /**
      * Failed effect overload
      */
    @inline def fail[T](bads: cd.NonEmptyList[Anomaly]): Validated[T] = ValidatedOps.fail(bads)

    @inline def unit: Validated[Unit] = ValidatedOps.unit

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def fromOptionAno[T](opt: Option[T], ifNone: => Anomaly): Validated[T] =
      ValidatedOps.fromOption(opt, ifNone)

    /**
      * Lift this [[scala.util.Try]] and  sequence its failure case [[java.lang.Throwable]] within this effect.
      * If the [[java.lang.Throwable]] is also an [[busymachines.core.Anomaly]] then it is used as is for the [[busymachines.effects.sync.Incorrect]] case,
      * but if it is not, then it is wrapped inside of a [[busymachines.core.CatastrophicError]] anomaly.
      *
      * If we have multiple [[busymachines.core.Anomalies]] then each individual [[Anomalies.messages]] is sequenced
      * withing this effect
      */
    @inline def fromTryAno[T](t: Try[T]): Validated[T] =
      ValidatedOps.fromTry(t)

    /**
      * Lift this [[busymachines.effects.sync.Result]] and  sequence its failure case within this effect.
      *
      * If we have multiple [[busymachines.core.Anomalies]] then each individual [[Anomalies.messages]] is sequenced
      * withing this effect
      */
    @inline def fromResult[T](t: Result[T]): Validated[T] =
      ValidatedOps.fromResult(t)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condAno[T](test: Boolean, good: => T, bad: => Anomaly): Validated[T] =
      ValidatedOps.cond(test, good, bad)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
      */
    @inline def condWith[T](test: Boolean, good: => Validated[T], bad: => Anomaly): Validated[T] =
      ValidatedOps.condWith(test, good, bad)

    /**
      * @return
      *   Failed effect, if the boolean is true
      */
    @inline def invalidOnTrue(test: Boolean, bad: => Anomaly): Validated[Unit] =
      ValidatedOps.invalidOnTrue(test, bad)

    /**
      * @return
      *   Failed effect, if the boolean is false
      */
    @inline def invalidOnFalse(test: Boolean, bad: => Anomaly): Validated[Unit] =
      ValidatedOps.invalidOnFalse(test, bad)

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */
    @inline def asOptionUnsafe[T](value: Validated[T]): Option[T] =
      ValidatedOps.asOptionUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
      *
      * Throws exceptions into your face
      *
      */
    @inline def asOptionUnsafe[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Option[T] =
      ValidatedOps.asOptionUnsafe(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * Throws exceptions into your face
      *
      */
    @inline def asListUnsafe[T](value: Validated[T]): List[T] =
      ValidatedOps.asListUnsafe(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
      *
      */
    @inline def asListUnsafe[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): List[T] =
      ValidatedOps.asListUnsafe(value, ctor)

    /**
      * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]] on the left
      * hand side is converted into a [[java.lang.Throwable]] and corresponds to a
      * failed [[scala.util.Try]]
      */
    @inline def asTry[T](value: Validated[T]): Try[T] =
      ValidatedOps.asTry(value)

    /**
      * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]]s are
      * transformed into an [[busymachines.core.Anomalies]] of your choice
      */
    @inline def asTry[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Try[T] =
      ValidatedOps.asTry(value, ctor)

    @inline def asResult[T](value: Validated[T]): Result[T] =
      ValidatedOps.asResult(value)

    @inline def asResult[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Result[T] =
      ValidatedOps.asResult(value, ctor)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    @inline def unsafeGet[T](value: Validated[T]): T =
      ValidatedOps.unsafeGet(value)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions of your choice in your face if the underlying effect is failed
      */
    @inline def unsafeGet[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): T =
      ValidatedOps.unsafeGet(value, ctor)

    /**
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. the computation captured within this effect are still executed,
      * it's just the final value that is discarded
      */
    @inline def discardContent[T](value: Validated[T]): Validated[Unit] =
      ValidatedOps.discardContent(value)

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * {{{
      * @inline def  checkIndex(i: Int): Validated[String] = ???
      *
      *   val fileIndex: List[Int] = List(0,1,2,3,4)
      *   val fileNames: Validated[List[String]] = Validated.traverse(fileIndex){ i =>
      *     checkIndex(i)
      *   }
      * }}}
      */
    @inline def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Validated[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]],
    ): Validated[C[B]] = ValidatedOps.traverse(col)(fn)

    /**
      * Basically like ``traverse`` but discards the value
      */
    @inline def traverse_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Validated[B]): Validated[Unit] =
      ValidatedOps.traverse_(col)(fn)

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * Specialized case of [[traverse]]
      *
      * {{{
      * @inline def  checkIndex(i: Int): Validated[String] = ???
      *
      *   val fileNamesTry: List[Validated[String]] = List(0,1,2,3,4).map(checkIndex)
      *   val fileNames:    Validated[List[String]] = Validated.sequence(fileNamesTry)
      * }}}
      */
    @inline def sequence[A, M[X] <: TraversableOnce[X]](in: M[Validated[A]])(
      implicit
      cbf: CanBuildFrom[M[Validated[A]], A, M[A]],
    ): Validated[M[A]] = ValidatedOps.sequence(in)

    /**
      * Like ``sequence`` but discards the value
      */
    @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Validated[A]]): Validated[Unit] =
      ValidatedOps.sequence_(in)

    @inline def sequence[A](head: Validated[A], tail: Validated[A]*): Validated[List[A]] =
      ValidatedOps.sequence(head, tail: _*)

    @inline def sequence_[A](head: Validated[A], tail: Validated[A]*): Validated[Unit] =
      ValidatedOps.sequence_(head, tail: _*)
  }
}

object ValidatedOps {

  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  /**
    * N.B. pass only pure values.
    */
  @inline def pure[T](value: T): Validated[T] = Validated.Valid(value)

  /**
    * Failed effect
    */
  @inline def fail[T](bad: Anomaly, bads: Anomaly*): Validated[T] = Validated.Invalid(cd.NonEmptyList.of(bad, bads: _*))

  /**
    * Failed effect overload
    */
  @inline def fail[T](bads: cd.NonEmptyList[Anomaly]): Validated[T] = Validated.Invalid(bads)

  val unit: Validated[Unit] = ValidatedOps.pure(())

  //===========================================================================
  //=================== Validated from various other effects ==================
  //===========================================================================

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def fromOption[T](opt: Option[T], ifNone: => Anomaly): Validated[T] = opt match {
    case None    => ValidatedOps.fail(ifNone)
    case Some(v) => ValidatedOps.pure(v)
  }

  /**
    * Lift this [[scala.util.Try]] and  sequence its failure case [[java.lang.Throwable]] within this effect.
    * If the [[java.lang.Throwable]] is also an [[busymachines.core.Anomaly]] then it is used as is for the [[busymachines.effects.sync.Incorrect]] case,
    * but if it is not, then it is wrapped inside of a [[busymachines.core.CatastrophicError]] anomaly.
    *
    * If we have multiple [[busymachines.core.Anomalies]] then each individual [[Anomalies.messages]] is sequenced
    * withing this effect
    */
  @inline def fromTry[T](t: Try[T]): Validated[T] = t match {
    case TryFailure(a: Anomalies) => ValidatedOps.fail(a.firstAnomaly, a.restOfAnomalies: _*)
    case TryFailure(a: Anomaly)   => ValidatedOps.fail(a)
    case TryFailure(NonFatal(r)) => ValidatedOps.fail(CatastrophicError(r))
    case TrySuccess(value)       => ValidatedOps.pure(value)
  }

  /**
    * Lift this [[busymachines.effects.sync.Result]] and  sequence its failure case within this effect.
    *
    * If we have multiple [[busymachines.core.Anomalies]] then each individual [[Anomalies.messages]] is sequenced
    * withing this effect
    */
  @inline def fromResult[T](t: Result[T]): Validated[T] = t match {
    case Incorrect(a: Anomalies) => ValidatedOps.fail(a.firstAnomaly, a.restOfAnomalies: _*)
    case Incorrect(a: Anomaly)   => ValidatedOps.fail(a)
    case Correct(value) => ValidatedOps.pure(value)
  }

  //===========================================================================
  //==================== Result from special cased Result =====================
  //===========================================================================

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def cond[T](test: Boolean, good: => T, bad: => Anomaly): Validated[T] =
    if (test) ValidatedOps.pure(good) else ValidatedOps.fail(bad)

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   failed effect with ``bad`` [[busymachines.core.Anomaly]] if boolean is false
    */
  @inline def condWith[T](test: Boolean, good: => Validated[T], bad: => Anomaly): Validated[T] =
    if (test) good else ValidatedOps.fail(bad)

  /**
    * @return
    *   Failed effect, if the boolean is true
    */
  @inline def invalidOnTrue(test: Boolean, bad: => Anomaly): Validated[Unit] =
    if (test) ValidatedOps.fail(bad) else ValidatedOps.unit

  /**
    * @return
    *   Failed effect, if the boolean is false
    */
  @inline def invalidOnFalse(test: Boolean, bad: => Anomaly): Validated[Unit] =
    if (!test) ValidatedOps.fail(bad) else ValidatedOps.unit

  //===========================================================================
  //====================== Validated to various effects =======================
  //===========================================================================

  /**
    * !!! USE WITH CARE !!!
    *
    * Throws exceptions into your face
    *
    */
  @inline def asOptionUnsafe[T](value: Validated[T]): Option[T] = value match {
    case cd.Validated.Valid(value) => OptionOps.pure(value)
    case cd.Validated.Invalid(e)   => throw GenericValidationFailures(e.head, e.tail)
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
    *
    * Throws exceptions into your face
    *
    */
  @inline def asOptionUnsafe[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Option[T] =
    value match {
      case cd.Validated.Valid(value) => OptionOps.pure(value)
      case cd.Validated.Invalid(e)   => throw ctor(e.head, e.tail).asThrowable
    }

  /**
    * !!! USE WITH CARE !!!
    *
    * Throws exceptions into your face
    *
    */
  @inline def asListUnsafe[T](value: Validated[T]): List[T] = value match {
    case cd.Validated.Valid(value) => List(value)
    case cd.Validated.Invalid(e)   => throw GenericValidationFailures(e.head, e.tail)
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Allows you to specify which specific [[busymachines.core.Anomalies]] to throw in your face.
    *
    */
  @inline def asListUnsafe[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): List[T] = value match {
    case cd.Validated.Valid(value) => List(value)
    case cd.Validated.Invalid(e)   => throw ctor(e.head, e.tail).asThrowable
  }

  /**
    * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]]s are
    * transformed into an [[GenericValidationFailures]]
    */
  @inline def asTry[T](value: Validated[T]): Try[T] = value match {
    case cd.Validated.Valid(value) => TryOps.pure(value)
    case cd.Validated.Invalid(e)   => TryOps.fail(GenericValidationFailures(e.head, e.tail))
  }

  /**
    * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]]s are
    * transformed into an [[busymachines.core.Anomalies]] of your choice
    */
  @inline def asTry[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Try[T] = value match {
    case cd.Validated.Valid(value) => TryOps.pure(value)
    case cd.Validated.Invalid(e)   => TryOps.fail(ctor(e.head, e.tail))
  }

  /**
    * Transforms this result into a [[busymachines.effects.sync.Result]]. The [[busymachines.core.Anomaly]]s are
    * transformed into an [[GenericValidationFailures]]
    */
  @inline def asResult[T](value: Validated[T]): Result[T] = value match {
    case cd.Validated.Valid(value) => Result.pure(value)
    case cd.Validated.Invalid(e)   => Result.fail(GenericValidationFailures(e.head, e.tail))
  }

  /**
    * Transforms this result into a [[scala.util.Try]]. The [[busymachines.core.Anomaly]]s are
    * transformed into an [[busymachines.core.Anomalies]] of your choice
    */
  @inline def asResult[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): Result[T] = value match {
    case cd.Validated.Valid(value) => Result.pure(value)
    case cd.Validated.Invalid(e)   => Result.fail(ctor(e.head, e.tail))
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Will throw exceptions in your face if the underlying effect is failed
    */
  @inline def unsafeGet[T](value: Validated[T]): T = value match {
    case cd.Validated.Valid(value) => value
    case cd.Validated.Invalid(e)   => throw GenericValidationFailures(e.head, e.tail)
  }

  /**
    * !!! USE WITH CARE !!!
    *
    * Will throw exceptions of your choice in your face if the underlying effect is failed
    */
  @inline def unsafeGet[T](value: Validated[T], ctor: (Anomaly, List[Anomaly]) => Anomalies): T = value match {
    case cd.Validated.Valid(value) => value
    case cd.Validated.Invalid(e)   => throw ctor(e.head, e.tail).asThrowable
  }

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  /**
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. the computation captured within this effect are still executed,
    * it's just the final value that is discarded
    */

  @inline def discardContent[T](value: Validated[T]): Validated[Unit] =
    value.map(ConstantsSyncEffects.UnitFunction1)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    * @inline def  checkIndex(i: Int): Validated[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: Validated[List[String]] = Validated.traverse(fileIndex){ i =>
    *     checkIndex(i)
    *   }
    * }}}
    */
  @inline def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Validated[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]],
  ): Validated[C[B]] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    import scala.collection.mutable

    if (col.isEmpty) {
      Validated.pure(cbf.apply().result())
    }
    else {
      //OK, super inneficient, need a better implementation
      val result:  Validated[List[B]]       = col.toList.traverse(fn)
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      result.map(_.foreach(e => builder.+=(e))).map(_ => builder.result())
    }
  }

  /**
    * Like ``traverse`` but discards the value
    */
  @inline def traverse_[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Validated[B]): Validated[Unit] = {
    import cats.instances.list._
    import cats.syntax.foldable._
    if (col.isEmpty) {
      Validated.unit
    }
    else {
      col.toList.traverse_(fn)
    }
  }

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    * @inline def  checkIndex(i: Int): Validated[String] = ???
    *
    *   val fileNamesTry: List[Validated[String]] = List(0,1,2,3,4).map(checkIndex)
    *   val fileNames:    Validated[List[String]] = Validated.sequence(fileNamesTry)
    * }}}
    */
  @inline def sequence[A, M[X] <: TraversableOnce[X]](in: M[Validated[A]])(
    implicit
    cbf: CanBuildFrom[M[Validated[A]], A, M[A]],
  ): Validated[M[A]] = ValidatedOps.traverse(in)(identity)

  /**
    *
    * Overload of generic ``sequence``
    *
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    * @inline def  checkIndex(i: Int): Validated[String] = ???
    *
    *   val fileNamesTry: List[Validated[String]] = List(0,1,2,3,4).map(checkIndex)
    *   val fileNames:    Validated[List[String]] = Validated.sequence(fileNamesTry)
    * }}}
    */
  @inline def sequence[A](head: Validated[A], tail: Validated[A]*): Validated[List[A]] =
    ValidatedOps.sequence(head :: tail.toList)

  /**
    * Like ``sequence`` but discards the value
    */
  @inline def sequence_[A, M[X] <: TraversableOnce[X]](in: M[Validated[A]]): Validated[Unit] =
    ValidatedOps.traverse_(in)(identity)

  /**
    * Like ``sequence`` but discards the value
    */
  @inline def sequence_[A](head: Validated[A], tail: Validated[A]*): Validated[Unit] =
    ValidatedOps.sequence_(head :: tail.toList)

}
