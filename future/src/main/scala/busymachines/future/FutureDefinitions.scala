package busymachines.future

import scala.{concurrent => sc}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
trait FutureDefinitions {
  type Future[T] = sc.Future[T]
  val Future: sc.Future.type = sc.Future

  type ExecutionContext = sc.ExecutionContext
  val ExecutionContext: sc.ExecutionContext.type = sc.ExecutionContext

  val Await: sc.Await.type = sc.Await

  implicit def bmCommonsUnsafeFutureOps[T](f: Future[T]): UnsafeFutureOps[T] = new UnsafeFutureOps[T](f)

  implicit def bmCommonsFutureCompanionOps: CompanionFutureOps.type = CompanionFutureOps

  implicit def bmCommonsSafeFutureOps[T](f: => Future[T]): SafeFutureOps[T] = new SafeFutureOps[T](f)
}
