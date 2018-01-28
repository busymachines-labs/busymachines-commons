package busymachines.effects

/**
  *
  * This is complementary syntax to [[busymachines.effects.result.ResultOps]]
  * it adds conversions to [[IO]] and [[Task]]
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait ResultEffectsSyntaxImplicits {

  implicit def bmCommonsResultOpsEffectsSyntax[T](r: Result[T]): ResultOpsEffectsSyntax[T] =
    new ResultOpsEffectsSyntax(r)

  implicit def bmCommonsSafeResultOpsEffectsSyntax[T](r: => Result[T]): SafeResultOpsEffectsSyntax[T] =
    new SafeResultOpsEffectsSyntax(r)

  implicit def bmCommonsResultCompanionOpsSyntax(r: Result.type): ResultCompanionOpsSyntax =
    new ResultCompanionOpsSyntax(r)
}

/**
  *
  * Imitates the style of [[Result.asFuture]] (et. all) methods.
  *
  * @param r
  *   unused
  */
final class ResultCompanionOpsSyntax(val r: Result.type) {
  def asIO[T](r: Result[T]): IO[T] = ResultEffectsUtil.asIO(r)

  def asTask[T](r: Result[T]): Task[T] = ResultEffectsUtil.asTask(r)

  def suspendInIO[T](r: => Result[T]): IO[T] = ResultEffectsUtil.suspendInIO(r)

  def suspendInTask[T](r: => Result[T]): Task[T] = ResultEffectsUtil.suspendInTask(r)
}

/**
  *
  *
  */
final class ResultOpsEffectsSyntax[T](private[this] val r: Result[T]) {

  def asIO: IO[T] = ResultEffectsUtil.asIO(r)

  def asTask: Task[T] = ResultEffectsUtil.asTask(r)
}

/**
  *
  *
  */
final class SafeResultOpsEffectsSyntax[T](r: => Result[T]) {

  def suspendInIO: IO[T] = ResultEffectsUtil.suspendInIO(r)

  def suspendInTask: Task[T] = ResultEffectsUtil.suspendInTask(r)
}

/**
  *
  */
object ResultEffectsUtil {

  def asIO[T](r: Result[T]): IO[T] = IOEffectsUtil.fromResult(r)

  def asTask[T](r: Result[T]): Task[T] = r match {
    case Correct(value)     => Task.pure(value)
    case Incorrect(anomaly) => Task.raiseError(anomaly.asThrowable)
  }

  /**
    * Use for those rare cases in which you suspect that functions returning Result
    * are not pure.
    *
    * Need for this is indicative of bugs in the functions you're calling
    *
    * Example usage:
    * {{{
    *   var sideEffect = 0
    *
    *   val suspendedSideEffect: IO[Int] = Result {
    *     println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    *     sideEffect = 42
    *     sideEffect
    *   }.suspendInIO
    *
    *  //this is not thrown:
    *  if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")
    * }}}
    */
  def suspendInIO[T](r: => Result[T]): IO[T] = IOEffectsUtil.fromResultSuspend(r)

  /**
    * Analagous to [[suspendInIO]]
    * Use for those rare cases in which you suspect that functions returning Result
    * are not pure.
    *
    * Need for this is indicative of bugs in the functions you're calling
    *
    * Example usage:
    * {{{
    *   var sideEffect = 0
    *
    *   val suspendedSideEffect: Task[Int] = Result {
    *     println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    *     sideEffect = 42
    *     sideEffect
    *   }.suspendInTask
    *
    *  //this is not thrown:
    *  if (sideEffect == 42) throw CatastrophicError("Side-effects make me sad")
    * }}}
    */
  def suspendInTask[T](r: => Result[T]): Task[T] = Task(r).flatMap(ResultEffectsUtil.asTask)
}
