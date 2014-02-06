package com.busymachines.prefab.media.elasticsearch

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import java.io.BufferedInputStream
import java.net.URL
import com.busymachines.commons.domain.CommonJsonFormats._
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.Media
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.Logging
import com.busymachines.commons.domain.Money
import com.busymachines.commons.domain.MimeTypes
import com.busymachines.prefab.media.db.MediaDao
import com.busymachines.commons.dao.SearchResult.toResult
import com.busymachines.commons.dao.Versioned.toEntity
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.prefab.media.domain.HashedMedia
import com.busymachines.prefab.media.domain.MediaDomainJsonFormats.hashMediaFormat

class ESMediaDao(index: ESIndex)(implicit ec: ExecutionContext) extends MediaDao with Logging {

  private val hasher = Hashing.md5
  private val encoding = BaseEncoding.base64Url
  private val dao = new ESRootDao[HashedMedia](index, ESType[HashedMedia]("media", MediaMapping))

  /**
   * Retrieves all medias stored.
   * @return
   */
  def retrieveAll: Future[List[Media]] =
    dao.retrieveAll map { medias =>
      medias.map(media =>
        media match {
          case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
            Media(Id(id.toString), mimeType, name, encoding.decode(data))
        })
    }

  /**
   * Deletes media by id.
   * @param id the media id
   * @return
   */
  def delete(id: Id[Media]): Future[Unit] =
    dao.delete(Id[HashedMedia](id.toString))

  /**
   * Retrieves a media object by id
   * @param id the media id
   * @return the media or None
   */
  def retrieve(id: Id[Media]): Future[Option[Media]] =
    dao.retrieve(Id[HashedMedia](id.toString)).map {
      _ map {
        case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
          Media(Id(id.toString), mimeType, name, encoding.decode(data))
      }
    }

  /**
   * Retrieves media by mime type & name & hashed data
   * @param mimeType the media mime type
   * @param name the media name
   * @param data the data
   * @return the media found or None
   */
  def retrieve(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Option[Media]] = {
    def hash = hasher.hashBytes(data).toString
    val stringData = encoding.encode(data)
    dao.search((MediaMapping.mimeType equ mimeType.value) or (MediaMapping.hash equ hash)) map {
      _.find(m => m.data == stringData && m.name == name) match {
        case Some(Versioned(HashedMedia(id, mimeType, name, hash, data), version)) =>
          Some(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
        case None => None
      }
    }
  }

  /**
   * Stores a media object that contains the specified data. Does automatic deduplication by name, mimeType & data hash.
   * @param mimeType the media mime type
   * @param name the media name
   * @param data the data
   * @return the media found or stored
   */
  def store(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Media] =
    retrieve(mimeType,name,data) flatMap {
        case Some(Versioned(HashedMedia(id, mimeType, name, hash, data), version)) =>
          Future.successful(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
        case None =>
          val id = Id.generate[Media]
          def hash = hasher.hashBytes(data).toString
          val stringData = encoding.encode(data)
          dao.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData)).map(_ => Media(id, mimeType, name, data))
      }

  /**
   * Imports a media object from a url. Does automatic deduplication.
   * @param url
   * @return
   */
  def importUrl(url: String): Future[Option[Media]] = {
    Future(readUrl(url)) flatMap {
      case Some(bytes) =>
        val name = url.substring(url.lastIndexOf('/') + 1)
        store(MimeTypes.fromResourceName(name), Some(name), bytes).map(Option(_))
      case None =>
        Future.successful(None)
    }
  }

  /**
   * Reads the a byte array from the specified url.
   * @param url the url
   * @return the byte array or None (if url not found)
   */
  def readUrl(url: String): Option[Array[Byte]] = {
    try {
      if (url.toString.isEmpty()) None
      else {
        val bis = new BufferedInputStream(new URL(url.toString).openStream())
        try {
          val bytes = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
          Some(bytes)
        } finally {
          bis.close
        }
      }
    } catch {
      case t: Throwable => debug(t); None
    }
  }
}
