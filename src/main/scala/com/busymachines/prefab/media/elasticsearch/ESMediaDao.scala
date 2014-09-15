package com.busymachines.prefab.media.elasticsearch

import java.io.BufferedInputStream
import java.net.URL
import com.busymachines.commons.Implicits._
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.dao.SearchResult.toResult
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.domain.{Id, MimeType, MimeTypes}
import com.busymachines.commons.elasticsearch.{ESIndex, ESRootDao, ESType}
import com.busymachines.prefab.media.db.MediaDao
import com.busymachines.prefab.media.domain.{HashedMedia, Media}
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import com.busymachines.commons.elasticsearch.ESCollection

class ESMediaDao(index: ESIndex, mimeTypeDetector:MimeTypeDetector)(implicit ec: ExecutionContext) extends MediaDao with Logging {

  private val collection = new ESCollection[HashedMedia](index, ESType[HashedMedia]("media", MediaMapping))

  @deprecated("Use ESCollection.scan", "0.6")
  def retrieveAll: Future[List[Media]] =
    collection.retrieveAll map { medias =>
      medias.map {
        case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
          Media(Id(id.toString), mimeType, name, data.decodeBase64Url)
      }
    }

  def delete(id: Id[Media]): Future[Unit] =
    collection.delete(id.value, refresh = true)

  def retrieve(id: Id[Media]): Future[Option[Media]] =
    collection.retrieve(id.value).map {
      _ map {
        case HashedMedia(id, mimeType, name, hash, data) =>
          Media(Id(id.toString), mimeType, name, data.decodeBase64Url)
      }
    }

  def retrieve(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Option[Media]] = {
    def hash = data.md5.encodeBase64
    val stringData = data.encodeBase64Url
    collection.search((MediaMapping.mimeType equ mimeType.value) or (MediaMapping.hash equ hash)) map {
      _.result.find(m => m.data == stringData && m.name == name) match {
        case Some(HashedMedia(id, mimeType, name, hash, data)) =>
          Some(Media(Id(id.toString), mimeType, name, data.decodeBase64Url))
        case None => None
      }
    }
  }

  def store(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Media] =
    retrieve(mimeType,name,data) flatMap {
        case Some(media) =>
          Future.successful(media)
        case None =>
          val id = Id.generate[Media]
          def hash = data.md5.encodeBase64
          val stringData = data.encodeBase64Url
          collection.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData), refresh = true).map(_ => Media(id, mimeType, name, data))
      }

  def importUrl(url: String): Future[Option[Media]] = {
    Future(readUrl(url)) flatMap {
      case Some(bytes) =>
        val name = url.substring(url.lastIndexOf('/') + 1)
        store(mimeTypeDetector.mimeTypeOf(Some(name),Some(bytes)).getOrElse(MimeTypes.fromResourceName(name)), Some(name), bytes).map(Option(_))
      case None =>
        Future.successful(None)
    }
  }

  def readUrl(url: String): Option[Array[Byte]] = {
    try {
      if (url.toString.isEmpty) None
      else {
        val bis = new BufferedInputStream(new URL(url.toString).openStream())
        try {
          val bytes = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
          Some(bytes)
        } finally {
          bis.close()
        }
      }
    } catch {
      case t: Throwable => logger.debug("readURL encountered an exception", t); None
    }
  }
}
