package busymachines.effects

import busymachines.core.Anomaly
import busymachines.future.FutureUtil

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
}
