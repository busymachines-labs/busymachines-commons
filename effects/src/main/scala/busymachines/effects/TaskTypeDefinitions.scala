package busymachines.effects

import monix.{execution => mex}
import monix.{eval      => mev}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait TaskTypeDefinitions {
  type CancellableFuture[T] = mex.CancelableFuture[T]

  type Scheduler = mex.Scheduler
  type Task[T]   = mev.Task[T]

  val Scheduler: mex.Scheduler.type = mex.Scheduler
  val Task:      mev.Task.type      = mev.Task

}
