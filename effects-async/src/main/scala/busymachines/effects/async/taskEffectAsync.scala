package busymachines.effects.async

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait TaskTypeDefinitions {
  import monix.{execution => mex}
  import monix.{eval      => mev}

  type CancellableFuture[T] = mex.CancelableFuture[T]

  /**
    * N.B.
    * that Scheduler is also a [[scala.concurrent.ExecutionContext]],
    * which makes this type the only implicit in context necessary to do
    * interop between [[Task]] and [[scala.concurrent.Future]]
    */
  type Scheduler = mex.Scheduler
  type Task[T]   = mev.Task[T]

  val Scheduler: mex.Scheduler.type = mex.Scheduler
  val Task:      mev.Task.type      = mev.Task

}
