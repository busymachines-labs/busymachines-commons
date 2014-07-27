package com.busymachines.prefab.media

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import org.scalatest.FlatSpec
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.Implicits._
import com.busymachines.commons.elasticsearch._
import com.busymachines.commons.Implicits.richFuture
import com.busymachines.commons.testing.EmptyESTestIndex
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Page
import com.busymachines.commons.Logging
import com.busymachines.commons.event.DoNothingEventSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.Some
import com.busymachines.commons.domain.GeoPoint
import com.busymachines.commons.elasticsearch.{ESRootDao, ESSearchSort}
import com.busymachines.prefab.media.elasticsearch.ESMediaDao
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.logic.DefaultMimeTypeDetector
import com.busymachines.commons.domain.MimeTypes

@RunWith(classOf[JUnitRunner])
class MediaDaoTests extends FlatSpec with Logging {

  "MediaDao" should "creates & & retrieve & avoids duplicates" in {

  val esIndex = new EmptyESTestIndex(getClass)

  lazy val mediaDao = new ESMediaDao(esIndex,mediaMimeTypeDetector)
  lazy val mediaMimeTypeDetector:MimeTypeDetector = DefaultMimeTypeDetector

  val media1 = mediaDao.store(MimeTypes.text, Some("testfile1"), "data".getBytes).await
  val media2 = mediaDao.store(MimeTypes.text, Some("testfile1"), "data".getBytes).await

  assert(media1.id === media2.id)
  }

}