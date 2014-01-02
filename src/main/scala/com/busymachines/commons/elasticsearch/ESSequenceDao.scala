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
import com.busymachines.commons.dao.SequenceDao
import com.busymachines.commons.dao.VersionConflictException
import com.busymachines.commons.dao.Versioned

private[elasticsearch] object SequenceMapping extends ESMapping[Sequence] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val value = "value" as Long
}

class ESSequenceDao(index: ESIndex, `type`: String = "sequence")(implicit ec: ExecutionContext)
  extends ESRootDao[Sequence](index, ESType[Sequence](`type`, SequenceMapping)) with SequenceDao with Logging {

  def current(sequence: Id[Sequence]): Future[Long] =
    retrieve(sequence).map(_.map(_.value).getOrElse(0))

  def next(sequence: Id[Sequence], incrementValue: Long, minimumValue: Long, retries: Int): Future[Long] =
    retry(increment(sequence, incrementValue, minimumValue), retries, 0).map(_.entity.value)

  private def increment(sequence: Id[Sequence], incrementValue: Long, minimumValue: Long): Future[Versioned[Sequence]] =
    getOrCreateAndModify(sequence, false)(Sequence(sequence, 0))(s => s.copy(value = Math.max(s.value + incrementValue, minimumValue)))
}
