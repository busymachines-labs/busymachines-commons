package busymachines.effects.sync

import busymachines.core.Anomaly

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

    def asTryWeak[T](value: Option[T], ifNone: => Throwable): Try[T] =
      OptionOps.asTryWeak(value, ifNone)

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

  }

  /**
    *
    */
  final class ReferenceOps[T](private[this] val value: Option[T]) {

    def asList: List[T] =
      value.toList

    def asTry(ifNone: => Anomaly): Try[T] =
      OptionOps.asTry(value, ifNone)

    def asTryWeak(ifNone: => Throwable): Try[T] =
      OptionOps.asTryWeak(value, ifNone)

    def asEither(ifNone: => Throwable): Either[Throwable, T] =
      OptionOps.asEither(value, ifNone)

    def asResult(ifNone: => Anomaly): Result[T] =
      OptionOps.asResult(value, ifNone)

    def unsafeGet(): T =
      OptionOps.unsafeGet(value)

    def morph[R](good: T => R, bad: => R): Option[R] =
      OptionOps.morph(value, good, bad)

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
  //======================= Option to various (pseudo)monads ======================
  //===========================================================================

  // —— def asList —— direct alias to option.toList

  def asTry[T](value: Option[T], ifNone: => Anomaly): Try[T] =
    TryOps.fromOption(value, ifNone)

  def asTryWeak[T](value: Option[T], ifNone: => Throwable): Try[T] =
    TryOps.fromOptionWeak(value, ifNone)

  def asEither[T](value: Option[T], ifNone: => Throwable): Either[Throwable, T] = value match {
    case Some(v) => Right(v)
    case None    => Left(ifNone)
  }

  def asResult[T](value: Option[T], ifNone: => Anomaly): Result[T] =
    ResultOps.fromOption(value, ifNone)

  def unsafeGet[T](value: Option[T]): T =
    value.get

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  def morph[T, R](value: Option[T], good: T => R, bad: => R): Option[R] = value match {
    case Some(v) => Some(good(v))
    case None    => Some(bad)
  }

}
