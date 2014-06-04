package com.busymachines.commons.test.prefab.media

import com.busymachines.commons.domain.MimeTypes
import com.busymachines.prefab.media.logic.DefaultMimeTypeDetector
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.elasticsearch.ESMediaDao
import com.busymachines.commons.event.DoNothingEventSystem
import com.busymachines.commons.testing.EmptyESTestIndex
import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions

class MediaDaoTests extends Specification with NoTimeConversions {
sequential

  "MediaDao" should {
    "create & retrieve & avoids duplicates" in {

      val esIndex = new EmptyESTestIndex(getClass, new DoNothingEventSystem)

      lazy val mediaDao = new ESMediaDao(esIndex,mediaMimeTypeDetector)
      lazy val mediaMimeTypeDetector:MimeTypeDetector = new DefaultMimeTypeDetector

      val media1 = Await.result(mediaDao.store(MimeTypes.text, Some("testfile1"), "data".getBytes), 1 minute)
      val media2 = Await.result(mediaDao.store(MimeTypes.text, Some("testfile1"), "data".getBytes), 1 minute)

      media1.id === media2.id
    }
  }

}