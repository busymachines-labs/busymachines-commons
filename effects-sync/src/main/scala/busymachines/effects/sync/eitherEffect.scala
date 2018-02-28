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

/**
  * This is extremely minimal, since you ought to use [[Result]], and this is here
  * to give a similar experience with the rest of the Ops, and most of that can be
  * achieved by using cats instead.
  * {{{
  *   import cats._, cats.implicits._
  * }}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 29 Jan 2018
  *
  */
object EitherSyntax {

  /**
    *
    */
  trait Implicits {
    implicit final def bmcEitherEffectReferenceOps[L, R](value: Either[L, R]): ReferenceOps[L, R] =
      new ReferenceOps(value)
  }

  /**
    *
    */
  final class ReferenceOps[L, R](val value: Either[L, R]) extends AnyVal {

    /**
      * !!! USE WITH CARE !!!
      *
      * Discards the left value
      *
      */
    @inline def asOptionUnsafe(): Option[R] =
      value.toOption

    /**
      * !!! USE WITH CARE !!!
      *
      * Discards the left value
      *
      */
    @inline def asListUnsafe(): List[R] = value match {
      case Right(good) => List(good)
      case _           => Nil
    }

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[Anomaly]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asTry(transformLeft: L => Anomaly): Try[R] =
      TryOps.fromEither(value, transformLeft)

    /**
      * Lift this [[Either]] and  sequence its left-hand-side [[Throwable]] within this effect
      * if it is a [[Throwable]].
      */
    @inline def asTryThr(implicit ev: L <:< Throwable): Try[R] =
      TryOps.fromEitherThr(value)(ev)

    /**
      * Lift this [[Either]] and transform its left-hand side into a [[Throwable]] and sequence it within
      * this effect, yielding a failed effect.
      */
    @inline def asTryThr(transformLeft: L => Throwable): Try[R] =
      TryOps.fromEitherThr(value, transformLeft)

    @inline def asResult(transformLeft: L => Anomaly): Result[R] =
      Result.fromEither(value, transformLeft)

    @inline def asResultThr(implicit ev: L <:< Throwable): Result[R] =
      Result.fromEitherThr(value)(ev)

    /**
      * Returns the value on the left.
      *
      * Throws exception if there isn't one
      */
    @inline def unsafeGetLeft(): L =
      value.left.get

    /**
      * Returns the value on the right.
      *
      * Throws exception if there isn't one
      */
    @inline def unsafeGetRight(): R =
      value.right.get
  }
}
