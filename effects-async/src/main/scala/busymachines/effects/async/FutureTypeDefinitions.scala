//package busymachines.effects.future
//
//import scala.{concurrent => sc}
//
///**
//  *
//  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
//  * @since 09 Jan 2018
//  *
//  */
//trait FutureTypeDefinitions {
//  type Future[T] = sc.Future[T]
//  val Future: sc.Future.type = sc.Future
//
//  type ExecutionContext = sc.ExecutionContext
//  val ExecutionContext: sc.ExecutionContext.type = sc.ExecutionContext
//
//  val Await: sc.Await.type = sc.Await
//
//  def blocking[T](body: => T): T = sc.blocking(body)
//
//}
