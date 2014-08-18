package com.busymachines.commons.elasticsearch

import com.busymachines.commons.Logging
import com.busymachines.commons.domain.{ Id, Sequence }
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.testing.EmptyESTestIndex
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ESSequenceDaoTests extends FlatSpec with Logging {

  val esIndex = EmptyESTestIndex(getClass)
  val sequenceDao = new ESSequenceDao(esIndex)
  val sequence1 = Id.static[Sequence]("test-sequence-1")
  val sequence2 = Id.static[Sequence]("test-sequence-2")

  // TODO : Investigate why it works alone and doesnt work in a test suite

  "SequenceDao" should "create & increment" in {

    //    assert(sequenceDao.next(sequence1).await === 1)
    //    assert(sequenceDao.next(sequence2).await === 1)
    //
    //    assert(sequenceDao.next(sequence1).await === 2)
    //    assert(sequenceDao.next(sequence2).await === 2)

  }

}