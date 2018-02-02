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

import busymachines.core.Anomaly

import scala.collection.generic.CanBuildFrom

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
object OptionSyntax {

  /**
    *
    */
  trait Implicits {
    implicit def bmcOptionCompanionObjectOps(obj: Option.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit def bmcOptionReferenceOps[T](value: Option[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit def bmcOptionBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit def bmcOptionNestedBooleanOps(test: Option[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Option.type) {

    //===========================================================================
    //========================== Primary constructors ===========================
    //===========================================================================

    def pure[T](value: T): Option[T] =
      OptionOps.pure(value)

    def some[T](value: T): Option[T] =
      OptionOps.some(value)

    def fail[T]: Option[T] = None

    def none[T]: Option[T] = None

    def unit: Option[Unit] = OptionOps.unit

    //===========================================================================
    //==================== Result from various other effects ====================
    //===========================================================================

    def fromTryUnsafe[T](value: Try[T]): Option[T] =
      value.toOption

    def fromEitherUnsafe[L, R](either: Either[L, R]): Option[R] =
      either.toOption

    def fromResultUnsafe[T](r: Result[T]): Option[T] =
      r.toOption

    def cond[T](test: Boolean, good: => T): Option[T] =
      OptionOps.cond(test, good)

    def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

    def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
      OptionOps.flatCond(test, good)

    def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
      OptionOps.flatCondWith(test, good)

    //===========================================================================
    //======================= Option to various other effects ===================
    //===========================================================================

    def asList[T](value: Option[T]): List[T] = value.toList

    def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    def unsafeGet[T](value: Option[T]): T =
      value.get

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    def recover[T](value: Option[T], ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Option[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): Option[C[B]] = OptionOps.traverse(col)(fn)

    def sequence[A, M[X] <: TraversableOnce[X]](in: M[Option[A]])(
      implicit
      cbf: CanBuildFrom[M[Option[A]], A, M[A]]
    ): Option[M[A]] = OptionOps.sequence(in)

  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Option[T]) {

    def asList: List[T] =
      value.toList

    def asTry(ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    def asTryThr(ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    def asEither(ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    def asResult(ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    def unsafeGet(): T =
      OptionOps.unsafeGet(value)

    def morph[R](good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    def recover(ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    def recoverWith(ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    def condOption[T](good: => T): Option[T] =
      OptionOps.cond(test, good)

    def condWithOption[T](good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Option[Boolean]) {

    def cond[T](good: => T): Option[T] =
      test.flatMap(b => OptionOps.cond(b, good))

    def condWith[T](good: => Option[T]): Option[T] =
      test.flatMap(b => OptionOps.condWith(b, good))
  }

}

/**
  *
  */
object OptionOps {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  def pure[T](t: T): Option[T] = Option(t)

  def some[T](t: T): Option[T] = Option(t)

  val unit: Option[Unit] = Option(())

  // —— apply delegates to Option.apply directly in syntax object

  //===========================================================================
  //==================== Option from various other effects =======================
  //===========================================================================

  // —— def fromTryUnsafe —— implemented directly as alias

  // —— def fromEitherUnsafe —— implemented directly as alias

  // —— def fromResultUnsafe —— implemented directly as alias

  //===========================================================================
  //======================== Option from special cased Option =======================
  //===========================================================================

  def cond[T](test: Boolean, good: => T): Option[T] =
    if (test) OptionOps.pure(good) else None

  def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
    if (test) good else None

  def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
    test.flatMap(b => OptionOps.cond(b, good))

  def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
    test.flatMap(b => OptionOps.condWith(b, good))

  //===========================================================================
  //========================= Option to various effects =======================
  //===========================================================================

  // —— def asList —— direct alias to option.toList

  def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
    TryOps.fromOption(value, ifNone)

  def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
    TryOps.fromOptionThr(value, ifNone)

  def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] = value match {
    case Some(v) => Right(v)
    case None    => Left(ifNone)
  }

  def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
    Result.fromOption(value, ifNone)

  def unsafeGet[T](value: Option[T]): T =
    value.get

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] = value match {
    case Some(v) => Option(good(v))
    case None    => Option(bad)
  }

  def recover[T](value: Option[T], ifNone: => T): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => Option(ifNone)
  }

  def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => ifNone
  }

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    *
    */
  def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Option[B])(
    implicit
    cbf: CanBuildFrom[C[A], B, C[B]]
  ): Option[C[B]] = {
    import scala.collection.mutable
    if (col.isEmpty) {
      OptionOps.pure(cbf.apply().result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.apply()
      val firstBuilder = fn(head) map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Option[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Option[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder flatMap [mutable.Builder[B, C[B]]] { (result: mutable.Builder[B, C[B]]) =>
            val f: Option[mutable.Builder[B, C[B]]] = fn(element) map { newElement =>
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

  /**
    *
    */
  def sequence[A, M[X] <: TraversableOnce[X]](in: M[Option[A]])(
    implicit
    cbf: CanBuildFrom[M[Option[A]], A, M[A]]
  ): Option[M[A]] = OptionOps.traverse(in)(identity)

}
