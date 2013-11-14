package com.busymachines.commons.test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import org.scalatest.FlatSpec
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.CommonJsonFormats._
import com.busymachines.commons.elasticsearch.ESSearchCriteria.Delegate
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.elasticsearch.ESSearchSort
import com.busymachines.commons.implicits.richFuture
import com.busymachines.commons.test.DomainJsonFormats.itemFormat
import com.busymachines.commons.test.DomainJsonFormats.propertyFormat
import com.busymachines.commons.testing.EmptyESTestIndex
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Page
import com.busymachines.commons.Logging
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.elasticsearch.ESSequenceDao
import com.busymachines.commons.domain.Sequence

class ESSequenceDaoTests extends FlatSpec with Logging {

  val esIndex = new EmptyESTestIndex(getClass, new DoNothingEventSystem)
  val sequenceDao = new ESSequenceDao(esIndex)
  val sequence1 = Sequence(Id.generate,"testSequence1",0)
  val sequence2 = Sequence(Id.generate,"testSequence2",0)
  
  "SequenceDao" should "create & increment" in {
    sequenceDao.create(sequence1)
    sequenceDao.create(sequence2)
    assert(sequenceDao.next(sequence1, 1) === 1)
    assert(sequenceDao.next(sequence2, 1) === 1)
    assert(sequenceDao.next(sequence1, 1) === 2)
    assert(sequenceDao.next(sequence2, 1) === 2)
  }
  
}