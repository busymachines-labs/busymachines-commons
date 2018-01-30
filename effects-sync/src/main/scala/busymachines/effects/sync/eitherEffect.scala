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
    implicit def bmcEitherEffectReferenceOps[L, R](value: Either[L, R]): ReferenceOps[L, R] =
      new ReferenceOps(value)
  }

  /**
    *
    */
  final class ReferenceOps[L, R](val value: Either[L, R]) extends AnyVal {

    def asOptionUnsafe(): Option[R] =
      value.toOption

    def asListUnsafe(): List[R] = value match {
      case Left(_)     => Nil
      case Right(good) => List(good)
    }

    def asTry(transformLeft: L => Anomaly): Try[R] =
      TryOps.fromEither(value, transformLeft)

    def asTryWeak(implicit ev: L <:< Throwable): Try[R] =
      TryOps.fromEitherWeak(value)(ev)

    def asTryWeak(transformLeft: L => Throwable): Try[R] =
      TryOps.fromEitherWeak(value, transformLeft)

    def asResult(transformLeft: L => Anomaly): Result[R] =
      Result.fromEither(value, transformLeft)

    def asResultWeak(implicit ev: L <:< Throwable): Result[R] =
      Result.fromEitherWeak(value)(ev)

    def unsafeGetLeft(): L = value.left.get

    def unsafeGetRight(): R = value.right.get
  }
}
