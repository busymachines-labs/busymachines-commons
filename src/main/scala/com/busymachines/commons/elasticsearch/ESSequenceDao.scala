package com.busymachines.commons.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import org.elasticsearch.index.engine.VersionConflictEngineException
import com.busymachines.commons.Logging
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.CommonJsonFormats.sequenceFormat
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Sequence
import scala.concurrent.Await

private[elasticsearch] object SequenceMapping extends ESMapping[Sequence] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val value = "value" as Long
}

class ESSequenceDao(index: ESIndex, `type` : String = "sequence")(implicit ec: ExecutionContext) 
    extends ESRootDao[Sequence](index, ESType[Sequence](`type`, SequenceMapping)) with Logging {

  def apply(name : String) : Future[Id[Sequence]] = {
    val id = Id[Sequence](name)
    getOrCreate(id, false)(Sequence(id, 1)).map(_.id)
  }
  
  def current(sequence : Id[Sequence]) : Future[Long] = 
    retrieve(sequence).map(_.map(_.value).getOrElse(0))
    
  def next(sequence : Id[Sequence], incrementValue : Long = 1, retries : Int = 50): Future[Long] = 
    retry(increment(sequence,incrementValue), retries, 0)

  private def retry(future: Future[Long], maxAttempts: Int, attempt: Int): Future[Long] = 
    future.recoverWith {
      case e: VersionConflictEngineException =>
        if (attempt > maxAttempts) Future.failed(e) 
        else retry(future, maxAttempts, attempt + 1)
    }

  private def increment(sequence: Id[Sequence], incrementValue:Long=1): Future[Long] = 
    getOrCreateAndModify(sequence, false)(Sequence(sequence, 0))(s => s.copy(value = s.value + incrementValue))
    .map (_.entity.value)
}
