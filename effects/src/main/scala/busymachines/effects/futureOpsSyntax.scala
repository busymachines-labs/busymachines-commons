package busymachines.effects

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
trait FutureEffectsSyntaxImplicits {

  implicit def bmCommonsFutureOpsEffectsSyntax[T](f: Future[T]): FutureOpsEffectsSyntax[T] =
    new FutureOpsEffectsSyntax(f)

  implicit def bmCommonsSafeFutureOpsEffectsSyntax[T](f: => Future[T]): SafeFutureOpsEffectsSyntax[T] =
    new SafeFutureOpsEffectsSyntax(f)

  implicit def bmCommonsFutureCompanionOpsSyntax(f: Future.type): FutureCompanionOpsSyntax =
    new FutureCompanionOpsSyntax(f)
}

final class FutureCompanionOpsSyntax(val fobj: Future.type) {
  def asIO[T](r: Future[T])(implicit ec: ExecutionContext): IO[T] = FutureEffectsUtil.asIO(r)

  def asTask[T](r: Future[T]): Task[T] = FutureEffectsUtil.asTask(r)

  def suspendInIO[T](r: => Future[T])(implicit ec: ExecutionContext): IO[T] = FutureEffectsUtil.suspendInIO(r)

  def suspendInTask[T](r: => Future[T]): Task[T] = FutureEffectsUtil.suspendInTask(r)
}

/**
  *
  *
  */
final class FutureOpsEffectsSyntax[T](private[this] val r: Future[T]) {

  def asIO(implicit ec: ExecutionContext): IO[T] = FutureEffectsUtil.asIO(r)

  def asTask: Task[T] = FutureEffectsUtil.asTask(r)
}

/**
  *
  *
  */
final class SafeFutureOpsEffectsSyntax[T](r: => Future[T]) {

  def suspendInIO(implicit ec: ExecutionContext): IO[T] = FutureEffectsUtil.suspendInIO(r)

  def suspendInTask: Task[T] = FutureEffectsUtil.suspendInTask(r)
}

/**
  *
  */
object FutureEffectsUtil {

  def suspendInIO[T](f: => Future[T])(implicit ec: ExecutionContext): IO[T] = IO.fromFuture(IO(f))

  def suspendInTask[T](f: => Future[T]): Task[T] = Task.deferFuture(f)

  /**
    * !!! USE WITH CAUTION !!!
    *
    * 99% of the time you actually want [[FutureEffectsUtil.suspendInIO]]
    *
    * This operation is not referentially transparent when Future is not created using
    * Future.successful, or Future.failed constructors,
    *
    * Read the docs of [[IO.fromFuture]] for more details.
    *
    * This ought to be used only, and only when you are certain that the future
    * you wish to transform is referentially transparent
    *
    */
  def asIO[T](f: Future[T])(implicit ec: ExecutionContext): IO[T] = IO.fromFuture(IO.pure(f))

  /**
    * !!! USE WITH CAUTION !!!
    *
    * 99% of the time you actually want [[FutureEffectsUtil.suspendInTask]]
    *
    * This operation is not referentially transparent when Future is not created using
    * Future.successful, or Future.failed constructors,
    *
    * Read the docs of [[Task.fromFuture]] for more details.
    *
    * This ought to be used only, and only when you are certain that the future
    * you wish to transform is referentially transparent
    *
    */
  def asTask[T](f: Future[T]): Task[T] = Task.fromFuture(f)
}
