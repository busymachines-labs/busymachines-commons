package busymachines.effects

import busymachines.core.Anomaly
import busymachines.effects.future.FutureUtil

import scala.util._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
trait OptionEffectsSyntaxImplicits {

  implicit def bmCommonsOptionEffectsSyntax[T](opt: Option[T]): OptionEffectsSyntax[T] =
    new OptionEffectsSyntax(opt)

  implicit def bmCommonsOptionEffectsCompanionSyntax(opt: Option.type): OptionEffectsCompanionSyntax =
    new OptionEffectsCompanionSyntax(opt)

  implicit def bmCommonsBooleanAsOptionEffectsSyntax(test: Boolean): BooleanAsOptionEffectsSyntax =
    new BooleanAsOptionEffectsSyntax(test)

  implicit def bmCommonsOptionBooleanAsOptionEffectsSyntax(test: Option[Boolean]): OptionBooleanAsOptionEffectsSyntax =
    new OptionBooleanAsOptionEffectsSyntax(test)
}

/**
  *
  */
final class OptionEffectsSyntax[T](val opt: Option[T]) extends AnyVal {

  /**
    * !!! Use with caution !!!
    *
    * Surpresses the underlying [[Throwable]] in case of failure
    *
    */
  def asList: List[T] = opt.toList

  def asTry(ifNone: => Anomaly): Try[T] = OptionEffectsUtil.asTry(opt, ifNone)

  def asTryWeak(ifNone: => Throwable): Try[T] = OptionEffectsUtil.asTryWeak(opt, ifNone)

  //asResult — already provided in the results module — eliding here

  //asFuture — already provided in the future module — eliding here

  def asTask(ifNone: => Anomaly): Task[T] = TaskEffectsUtil.fromOption(opt, ifNone)

  def asIO(ifNote: => Anomaly): IO[T] = IOEffectsUtil.fromOption(opt, ifNote)

  /**
    * Simple alias to signal how horrible it is
    */
  def unsafeGet: T = opt.get

  def morph[R](some: T => R, ifNone: => R): Option[R] =
    OptionEffectsUtil.morph(opt, some, ifNone)
}

/**
  *
  */
final class BooleanAsOptionEffectsSyntax(val test: Boolean) extends AnyVal {

  def condOption[T](whenTrue: => T): Option[T] =
    OptionEffectsUtil.cond(test, whenTrue)

  def condWithOption[T](whenTrue: => Option[T]): Option[T] =
    OptionEffectsUtil.condWith(test, whenTrue)
}

/**
  *
  */
final class OptionBooleanAsOptionEffectsSyntax(val test: Option[Boolean]) extends AnyVal {

  def cond[T](whenTrue: => T): Option[T] =
    OptionEffectsUtil.flatCond(test, whenTrue)

  def condWith[T](whenTrue: => Option[T]): Option[T] =
    OptionEffectsUtil.flatCondWith(test, whenTrue)
}

/**
  *
  */
final class OptionEffectsCompanionSyntax(val optObj: Option.type) extends AnyVal {

  /**
    * !!! Use with caution !!!
    *
    * Surpresses the underlying [[Throwable]] in case of failure
    *
    */
  def asList[T](opt: Option[T]): List[T] = opt.toList

  def asTry[T](opt: Option[T], ifNone: => Anomaly): Try[T] = OptionEffectsUtil.asTry(opt, ifNone)

  def asTryWeak[T](opt: Option[T], ifNone: => Throwable): Try[T] = OptionEffectsUtil.asTryWeak(opt, ifNone)

  def asResult[T](opt: Option[T], ifNone: => Anomaly): Result[T] = Result.fromOption(opt, ifNone)

  def asFuture[T](opt: Option[T], ifNone: => Anomaly): Future[T] = FutureUtil.fromOption(opt, ifNone)

  def asTask[T](opt: Option[T], ifNone: => Anomaly): Task[T] = TaskEffectsUtil.fromOption(opt, ifNone)

  def asIO[T](opt: Option[T], ifNote: => Anomaly): IO[T] = IOEffectsUtil.fromOption(opt, ifNote)

  //===========================================================================
  //===================== Option from special cased Option ====================
  //===========================================================================

  def cond[T](test: Boolean, whenTrue: => T): Option[T] =
    OptionEffectsUtil.cond(test, whenTrue)

  def condWith[T](test: Boolean, whenTrue: => Option[T]): Option[T] =
    OptionEffectsUtil.condWith(test, whenTrue)

  def flatCond[T](test: Option[Boolean], whenTrue: => T): Option[T] =
    OptionEffectsUtil.flatCond(test, whenTrue)

  def flatCondWith[T](test: Option[Boolean], whenTrue: => Option[T]): Option[T] =
    OptionEffectsUtil.flatCondWith(test, whenTrue)

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  // — Option.bimap does not make sense — not included

  def morph[T, R](opt: Option[T], some: T => R, ifNone: => R): Option[R] =
    OptionEffectsUtil.morph(opt, some, ifNone)
}

object OptionEffectsUtil {

  def asTry[T](opt: Option[T], anomaly: => Anomaly): Try[T] = opt match {
    case Some(value) => Success(value)
    case None        => Failure(anomaly.asThrowable)
  }

  def asTryWeak[T](opt: Option[T], throwable: => Throwable): Try[T] = opt match {
    case Some(value) => Success(value)
    case None        => Failure(throwable)
  }

  def cond[T](test: Boolean, whenTrue: => T): Option[T] =
    if (test) Option(whenTrue) else Option.empty[T]

  def condWith[T](test: Boolean, whenTrue: => Option[T]): Option[T] =
    if (test) whenTrue else Option.empty[T]

  def flatCond[T](test: Option[Boolean], whenTrue: => T): Option[T] =
    test flatMap (b => OptionEffectsUtil.cond(b, whenTrue))

  def flatCondWith[T](test: Option[Boolean], whenTrue: => Option[T]): Option[T] =
    test flatMap (b => OptionEffectsUtil.condWith(b, whenTrue))

  //===========================================================================
  //============================== Transformers ===============================
  //===========================================================================

  // — Option.bimap does not make sense — not included

  def morph[T, R](opt: Option[T], some: T => R, ifNone: => R): Option[R] =
    opt match {
      case None        => Option(ifNone)
      case Some(value) => Option(some(value))
    }
}
