package com.busymachines.commons.dao

import com.busymachines.commons.domain.Sequence
import scala.concurrent.Future
import com.busymachines.commons.domain.Id

/**
 * Used on the application side to implement global sequence numbers on persistence layer with optimistic
 * concurrency control.
 */
trait SequenceDao extends RootDao[Sequence] {

  /**
   *
   * @param sequence
   * @return
   */
  def current(sequence : Id[Sequence]): Future[Long] 
  def next(sequence : Id[Sequence], incrementValue : Long = 1, minimumValue : Long = 1, retries : Int = 50): Future[Long]  

}