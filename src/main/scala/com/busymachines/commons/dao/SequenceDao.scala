package com.busymachines.commons.dao

import com.busymachines.commons.domain.Sequence
import scala.concurrent.Future
import com.busymachines.commons.domain.Id

trait SequenceDao extends RootDao[Sequence] {

  def apply(name : String) : Future[Id[Sequence]] 
  def current(sequence : Id[Sequence]) : Future[Long] 
  def next(sequence : Id[Sequence], incrementValue : Long = 1, retries : Int = 50): Future[Long]  

}