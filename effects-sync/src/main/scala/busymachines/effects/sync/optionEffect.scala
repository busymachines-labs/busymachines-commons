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

import scala.collection.compat._

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
    implicit final def bmcOptionCompanionObjectOps(obj: Option.type): CompanionObjectOps =
      new CompanionObjectOps(obj)

    implicit final def bmcOptionReferenceOps[T](value: Option[T]): ReferenceOps[T] =
      new ReferenceOps(value)

    implicit final def bmcOptionBooleanOps(test: Boolean): BooleanOps =
      new BooleanOps(test)

    implicit final def bmcOptionNestedBooleanOps(test: Option[Boolean]): NestedBooleanOps =
      new NestedBooleanOps(test)
  }

  /**
    *
    */
  final class CompanionObjectOps(val obj: Option.type) extends AnyVal {

    //===========================================================================
    //========================== Primary constructors ===========================
    //===========================================================================

    /**
      * N.B. pass only pure values
      */
    @inline def pure[T](value: T): Option[T] =
      OptionOps.pure(value)

    /**
      * N.B. pass only pure values
      */
    @inline def some[T](value: T): Option[T] =
      OptionOps.some(value)

    /**
      * Failed effect
      */
    @inline def fail[T]: Option[T] =
      None

    /**
      * Failed effect
      */
    @inline def none[T]: Option[T] =
      None

    @inline def unit: Option[Unit] =
      OptionOps.unit

    //===========================================================================
    //==================== Result from various other effects ====================
    //===========================================================================

    /**
      * Returns `None` if this is a `Failure` or a `Some` containing the value if this is a `Success`.
      */
    @inline def fromTryUnsafe[T](value: Try[T]): Option[T] =
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
    @inline def fromEitherUnsafe[L, R](either: Either[L, R]): Option[R] =
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
    @inline def fromResultUnsafe[T](r: Result[T]): Option[T] =
      r.toOption

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def cond[T](test: Boolean, good: => T): Option[T] =
      OptionOps.cond(test, good)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
      OptionOps.flatCond(test, good)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
      OptionOps.flatCondWith(test, good)

    //===========================================================================
    //======================= Option to various other effects ===================
    //===========================================================================

    /**
      * Returns a singleton list containing the $option's value
      * if it is nonempty, or the empty list if the $option is empty.
      */
    @inline def asList[T](value: Option[T]): List[T] =
      value.toList

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    @inline def unsafeGet[T](value: Option[T]): T =
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
    @inline def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    /**
      * If the underlying option is [[scala.None]], then make this effect Some(ifNone).
      */
    @inline def recover[T](value: Option[T], ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    /**
      * If the underlying option is [[scala.None]], then make this effect equal to
      * the returned one
      */
    @inline def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. the computation captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent[T](value: Option[T]): Option[Unit] =
      OptionOps.discardContent(value)

    //=========================================================================
    //=============================== Traversals ==============================
    //=========================================================================

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * {{{
      * @inline def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileIndex: List[Int] = List(0,1,2,3,4)
      *   val fileNames: Option[List[String]] = Option.traverse(fileIndex){ i =>
      *     indexToFilename(i)
      *   }
      * }}}
      */
    @inline def traverse[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => Option[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): Option[C[B]] = OptionOps.traverse(col)(fn)

    /**
      * Similar to [[traverse]], but discards all content. i.e. used only
      * for the combined effects.
      *
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * {{{
      * @inline def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileIndex: List[Int] = List(0,1,2,3,4)
      *   val fileNames: Option[List[String]] = Option.traverse_(fileIndex){ i =>
      *     indexToFilename(i)
      *   }
      * }}}
      */
    @inline def traverse_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => Option[B])(
      implicit
      cbf: BuildFrom[C[A], B, C[B]],
    ): Option[Unit] = OptionOps.traverse_(col)(fn)

    /**
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * Specialized case of [[traverse]]
      *
      * {{{
      * @inline def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
      *   val fileNames:       Option[List[String]] = Option.sequence(fileNamesOption)
      * }}}
      */
    @inline def sequence[A, M[X] <: IterableOnce[X]](in: M[Option[A]])(
      implicit
      cbf: BuildFrom[M[Option[A]], A, M[A]],
    ): Option[M[A]] = OptionOps.sequence(in)

    /**
      * Similar to [[sequence]], but discards all content. i.e. used only
      * for the combined effects.
      * see:
      * https://typelevel.org/cats/api/cats/Traverse.html
      *
      * Specialized case of [[traverse]]
      *
      * {{{
      * @inline def indexToFilename(i: Int): Option[String] = ???
      *
      *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
      *   val fileNames:       Option[Unit] = Option.sequence_(fileNamesOption)
      * }}}
      */
    @inline def sequence_[A, M[X] <: IterableOnce[X]](in: M[Option[A]])(
      implicit
      cbf: BuildFrom[M[Option[A]], A, M[A]],
    ): Option[Unit] = OptionOps.sequence_(in)

  }

  /**
    *
    */
  final class ReferenceOps[T](val value: Option[T]) extends AnyVal {

    /**
      * Returns a singleton list containing the $option's value
      * if it is nonempty, or the empty list if the $option is empty.
      */
    @inline def asList: List[T] =
      value.toList

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTry(ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asTryThr(ifNone: => Throwable): Try[T] =
      OptionOps.asTryThr(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asEither(ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    /**
      * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
      */
    @inline def asResult(ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    /**
      * !!! USE WITH CARE !!!
      *
      * Will throw exceptions in your face if the underlying effect is failed
      */
    @inline def unsafeGet(): T =
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
    @inline def morph[R](good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

    /**
      * If the underlying option is [[scala.None]], then make this effect Some(ifNone).
      */
    @inline def recover(ifNone: => T): Option[T] =
      OptionOps.recover(value, ifNone)

    /**
      * If the underlying option is [[scala.None]], then make this effect equal to
      * the returned one
      */
    @inline def recoverWith(ifNone: => Option[T]): Option[T] =
      OptionOps.recoverWith(value, ifNone)

    /**
      *
      * Explicitely discard the contents of this effect, and return [[Unit]] instead.
      *
      * N.B. the computation captured within this effect are still executed,
      * it's just the final value that is discarded
      *
      */
    @inline def discardContent: Option[Unit] =
      OptionOps.discardContent(value)
  }

  /**
    *
    *
    */
  final class BooleanOps(val test: Boolean) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def condOption[T](good: => T): Option[T] =
      OptionOps.cond(test, good)

    /**
      * @return
      *   effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def condWithOption[T](good: => Option[T]): Option[T] =
      OptionOps.condWith(test, good)

  }

  /**
    *
    *
    */
  final class NestedBooleanOps(val test: Option[Boolean]) extends AnyVal {

    /**
      * @return
      *   pure effect from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def cond[T](good: => T): Option[T] =
      OptionOps.flatCond(test, good)

    /**
      * @return
      *   effect resulted from ``good`` if the boolean is true
      *   [[scala.None]] if false, or if effect is [[scala.None]]
      */
    @inline def condWith[T](good: => Option[T]): Option[T] =
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
  @inline def pure[T](t: T): Option[T] =
    Option(t)

  /**
    * N.B. pass only pure values
    */
  @inline def some[T](t: T): Option[T] =
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
    *   [[scala.None]] if false, or if effect is [[scala.None]]
    */
  @inline def cond[T](test: Boolean, good: => T): Option[T] =
    if (test) OptionOps.pure(good) else None

  /**
    * @return
    *   effect from ``good`` if the boolean is true
    *   [[scala.None]] if false, or if effect is [[scala.None]]
    */
  @inline def condWith[T](test: Boolean, good: => Option[T]): Option[T] =
    if (test) good else None

  /**
    * @return
    *   pure effect from ``good`` if the boolean is true
    *   [[scala.None]] if false, or if effect is [[scala.None]]
    */
  @inline def flatCond[T](test: Option[Boolean], good: => T): Option[T] =
    test.flatMap(b => OptionOps.cond(b, good))

  /**
    * @return
    *   effect resulted from ``good`` if the boolean is true
    *   [[scala.None]] if false, or if effect is [[scala.None]]
    */
  @inline def flatCondWith[T](test: Option[Boolean], good: => Option[T]): Option[T] =
    test.flatMap(b => OptionOps.condWith(b, good))

  //===========================================================================
  //========================= Option to various effects =======================
  //===========================================================================

  // —— def asList —— direct alias to option.toList

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
    TryOps.fromOption(value, ifNone)

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def asTryThr[T](value: Option[T], ifNone: => Throwable): Try[T] =
    TryOps.fromOptionThr(value, ifNone)

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] = value match {
    case Some(v) => Right(v)
    case None    => Left(ifNone)
  }

  /**
    * Lift this [[Option]] and transform it into a failed effect if it is [[scala.None]]
    */
  @inline def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
    Result.fromOption(value, ifNone)

  /**
    * !!! USE WITH CARE !!!
    *
    * Will throw exceptions in your face if the underlying effect is failed
    */
  @inline def unsafeGet[T](value: Option[T]): T =
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
  @inline def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] = value match {
    case Some(v) => Option(good(v))
    case None    => Option(bad)
  }

  /**
    * If the underlying option is [[scala.None]], then make this effect Some(ifNone).
    */
  @inline def recover[T](value: Option[T], ifNone: => T): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => Option(ifNone)
  }

  /**
    * If the underlying option is [[scala.None]], then make this effect equal to
    * the returned one
    */
  @inline def recoverWith[T](value: Option[T], ifNone: => Option[T]): Option[T] = value match {
    case Some(v) => Option(v)
    case None    => ifNone
  }

  /**
    *
    * Explicitely discard the contents of this effect, and return [[Unit]] instead.
    *
    * N.B. the computation captured within this effect are still executed,
    * it's just the final value that is discarded
    *
    */
  @inline def discardContent[T](value: Option[T]): Option[Unit] =
    value.map(ConstantsSyncEffects.UnitFunction1)

  //=========================================================================
  //=============================== Traversals ==============================
  //=========================================================================

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    * @inline def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: Option[List[String]] = Option.traverse(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
    */
  @inline def traverse[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => Option[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): Option[C[B]] = {
    import scala.collection.mutable
    if (col.iterator.isEmpty) {
      OptionOps.pure(cbf.newBuilder(col).result())
    }
    else {
      val seq  = col.toSeq
      val head = seq.head
      val tail = seq.tail
      val builder: mutable.Builder[B, C[B]] = cbf.newBuilder(col)
      val firstBuilder = fn(head).map { z =>
        builder.+=(z)
      }
      val eventualBuilder: Option[mutable.Builder[B, C[B]]] = tail.foldLeft(firstBuilder) {
        (serializedBuilder: Option[mutable.Builder[B, C[B]]], element: A) =>
          serializedBuilder.flatMap[mutable.Builder[B, C[B]]] { (result: mutable.Builder[B, C[B]]) =>
            val f: Option[mutable.Builder[B, C[B]]] = fn(element).map { newElement =>
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
    * Similar to [[traverse]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * {{{
    * @inline def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileIndex: List[Int] = List(0,1,2,3,4)
    *   val fileNames: Option[List[String]] = Option.traverse_(fileIndex){ i =>
    *     indexToFilename(i)
    *   }
    * }}}
    */
  @inline def traverse_[A, B, C[X] <: IterableOnce[X]](col: C[A])(fn: A => Option[B])(
    implicit
    cbf: BuildFrom[C[A], B, C[B]],
  ): Option[Unit] = OptionOps.discardContent(OptionOps.traverse(col)(fn))

  /**
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    * @inline def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames:       Option[List[String]] = Option.sequence(fileNamesOption)
    * }}}
    */
  @inline def sequence[A, M[X] <: IterableOnce[X]](in: M[Option[A]])(
    implicit
    cbf: BuildFrom[M[Option[A]], A, M[A]],
  ): Option[M[A]] = OptionOps.traverse(in)(identity)

  /**
    * Similar to [[sequence]], but discards all content. i.e. used only
    * for the combined effects.
    *
    * see:
    * https://typelevel.org/cats/api/cats/Traverse.html
    *
    * Specialized case of [[traverse]]
    *
    * {{{
    * @inline def indexToFilename(i: Int): Option[String] = ???
    *
    *   val fileNamesOption: List[Option[String]] = List(0,1,2,3,4).map(indexToFileName)
    *   val fileNames:       Option[Unit] = Option.sequence_(fileNamesOption)
    * }}}
    */
  @inline def sequence_[A, M[X] <: IterableOnce[X]](in: M[Option[A]])(
    implicit
    cbf: BuildFrom[M[Option[A]], A, M[A]],
  ): Option[Unit] = OptionOps.discardContent(OptionOps.sequence(in))

}
