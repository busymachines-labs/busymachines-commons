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

    /**
      * N.B. pass only pure values
      */
    def pure[T](value: T): Option[T] =
      OptionOps.pure(value)

    /**
      * N.B. pass only pure values
      */
    def some[T](value: T): Option[T] =
      OptionOps.some(value)

    /**
      * Failed effect
      */
    def fail[T]: Option[T] =
      None

    /**
      * Failed effect
      */
    def none[T]: Option[T] =
      None

    def unit: Option[Unit] =
      OptionOps.unit

    //===========================================================================
    //==================== Result from various other effects ====================
    //===========================================================================

    /**
      * Returns `None` if this is a `Failure` or a `Some` containing the value if this is a `Success`.
      */
    def fromTryUnsafe[T](value: Try[T]): Option[T] =
      value.toOption

    /**
      * Returns a `Some` containing the `Right` value
      *  if it exists or a `None` if this is a `Left`.
      *
      * {{{
      * Right(12).toOption // Some(12)
      * Left(12).toOption  // None
      * }}}
      */
    def fromEitherUnsafe[L, R](either: Either[L, R]): Option[R] =
      either.toOption

    /**
      * Returns a `Some` containing the `Correct` value
      *  if it exists or a `None` if this is a `Incorrect`.
      *
      * {{{
      * Correct(12).toOption // Some(12)
      * Incorrect(12).toOption  // None
      * }}}
      */
    def fromResultUnsafe[T](r: Result[T]): Option[T] =
      r.toOption

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def cond[T](test: Boolean, good: => T): Option[T] =
      OptionOps.cond(test, good)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
      OptionOps.flatCond(test, good)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
      OptionOps.flatCondWith(test, good)

    //===========================================================================
    //======================= Option to various other effects ===================
    //===========================================================================

    /**
      * Returns a singleton list containing the $option's value
      * if it is nonempty, or the empty list if the $option is empty.
      */
    def asList[T](value: Option[T]): List[T] =
      value.toList

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    def unsafeGet[T](value: Option[T]): T =
      value.get

    //===========================================================================
    //============================== Transformers ===============================
    //===========================================================================

    /**
      *
      * Given the basic two-pronged nature of this effect.
      * the ``good`` function transforms the underlying "pure" (some) if that's the case.
      * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
      *
      * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
      * that does not short-circuit monadic flatMap chains.
      *
      * e.g:
      * {{{
      *   val o: Option[Int] = Option.none
      *   Option.morph(o, (i: Int) => i *2, 42)
      * }}}
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    /**
      * If the underlying option is [[None]], then make this effect Some(ifNone).
      */
    def recover[T](value: Option[T], ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    /**
      * If the underlying option is [[None]], then make this effect equal to
      * the returned one
      */
    def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * {{{
      *   def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileIndex: List[Int] = List(0,1,2,3,4)
      *   val fileNames: Option[List[String]] = Option.traverse(fileIndex){ i =>
      *     indexToFilename(i)
      *   }
      * }}}
      */
    def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => Option[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): Option[C[B]] = OptionOps.traverse(col)(fn)

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * Specialized case of [[traverse]]
      *
      * {{{
      *   def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
      *   val fileNames:       Option[List[String]] = Option.sequence(fileNamesOption)
      * }}}
      */
    def sequence[A, M[X] <: TraversableOnce[X]](in: M[Option[A]])(
      implicit
      cbf: CanBuildFrom[M[Option[A]], A, M[A]]
    ): Option[M[A]] = OptionOps.sequence(in)

  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Option[T]) {

    /**
      * Returns a singleton list containing the $option's value
      * if it is nonempty, or the empty list if the $option is empty.
      */
    def asList: List[T] =
      value.toList

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asTry(ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asTryThr(ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asEither(ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
      */
    def asResult(ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    def unsafeGet(): T =
      OptionOps.unsafeGet(value)

    /**
      *
      * Given the basic two-pronged nature of this effect.
      * the ``good`` function transforms the underlying "pure" (some) if that's the case.
      * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
      *
      * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
      * that does not short-circuit monadic flatMap chains.
      *
      * e.g:
      * {{{
      *   val o: Option[Int] = Option.none
      *   Option.morph(o, (i: Int) => i *2, 42)
      * }}}
      *
      * Undefined behavior if you throw exceptions in the method. DO NOT do that!
      */
    def morph[R](good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    /**
      * If the underlying option is [[None]], then make this effect Some(ifNone).
      */
    def recover(ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    /**
      * If the underlying option is [[None]], then make this effect equal to
      * the returned one
      */
    def recoverWith(ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)
  }

  /**
    *
    *
    */
  final class BooleanOps(private[this] val test: Boolean) {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def condOption[T](good: => T): Option[T] =
      OptionOps.cond(test, good)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def condWithOption[T](good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(private[this] val test: Option[Boolean]) {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def cond[T](good: => T): Option[T] =
      OptionOps.flatCond(test, good)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   [[None]] if false, or if effect is [[None]]
      */
    def condWith[T](good: => Option[T]): Option[T] =
      OptionOps.flatCondWith(test, good)
  }

}

/**
  *
  */
object OptionOps {
  //===========================================================================
  //========================== Primary constructors ===========================
  //===========================================================================

  /**
    * N.B. pass only pure values
    */
  def pure[T](t: T): Option[T] =
    Option(t)

  /**
    * N.B. pass only pure values
    */
  def some[T](t: T): Option[T] =
    Option(t)

  val unit: Option[Unit] =
    Option(())

  // —— apply delegates to Option.apply directly in syntax object

  //===========================================================================
  //==================== Option from various other effects =======================
  //===========================================================================

  // —— def fromTryUnsafe —— implemented directly as alias

  // —— def fromEitherUnsafe —— implemented directly as alias

  // —— def fromResultUnsafe —— implemented directly as alias

  //===========================================================================
  //===================== Option from special cased Option ====================
  //===========================================================================

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   [[None]] if false, or if effect is [[None]]
    */
  def cond[T](test: Boolean, good: => T): Option[T] =
    if (test) OptionOps.pure(good) else None

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   [[None]] if false, or if effect is [[None]]
    */
  def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
    if (test) good else None

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   [[None]] if false, or if effect is [[None]]
    */
  def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
    test.flatMap(b => OptionOps.cond(b, good))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   [[None]] if false, or if effect is [[None]]
    */
  def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
    test.flatMap(b => OptionOps.condWith(b, good))

  //===========================================================================
  //========================= Option to various effects =======================
  //===========================================================================

  // —— def asList —— direct alias to option.toList

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
    TryOps.fromOption(value, ifNone)

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
    TryOps.fromOptionThr(value, ifNone)

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] = value match {
    case Some(v) => Right(v)
    case None    => Left(ifNone)
  }

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[None]]
    */
  def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
    Result.fromOption(value, ifNone)

  /**
    * !!! USE WITH CARE !!!
    *
    * Will throw exceptions in your face if the underlying effect is failed
    */
  def unsafeGet[T](value: Option[T]): T =
    value.get

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  /**
    *
    * Given the basic two-pronged nature of this effect.
    * the ``good`` function transforms the underlying "pure" (some) if that's the case.
    * the ``bad`` function transforms the underlying "failure" part of the effect into a "pure" part.
    *
    * Therefore, by using ``morph`` you are defining the rules by which to make the effect into a successful one
    * that does not short-circuit monadic flatMap chains.
    *
    * e.g:
    * {{{
    *   val o: Option[Int] = Option.none
    *   Option.morph(o, (i: Int) => i *2, 42)
    * }}}
    *
    * Undefined behavior if you throw exceptions in the method. DO NOT do that!
    */
  def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] = value match {
    case Some(v) => Option(good(v))
    case None    => Option(bad)
  }

  /**
    * If the underlying option is [[None]], then make this effect Some(ifNone).
    */
  def recover[T](value: Option[T], ifNone: => T): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => Option(ifNone)
  }

  /**
    * If the underlying option is [[None]], then make this effect equal to
    * the returned one
    */
  def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => ifNone
  }

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    *   def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: Option[List[String]] = Option.traverse(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
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
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    *   def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames:       Option[List[String]] = Option.sequence(fileNamesOption)
    * }}}
    */
  def sequence[A, M[X] <: TraversableOnce[X]](in: M[Option[A]])(
    implicit
    cbf: CanBuildFrom[M[Option[A]], A, M[A]]
  ): Option[M[A]] = OptionOps.traverse(in)(identity)

}
