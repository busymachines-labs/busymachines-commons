package com.busymachines.commons.elasticsearch

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.elasticsearch.index.query.FilterBuilders.termFilter
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import com.busymachines.commons
import java.io.BufferedInputStream
import java.net.URL
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.domain.CommonJsonFormats._
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.Media
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.Logging
import com.busymachines.commons.domain.Money
import com.busymachines.commons.domain.MimeTypes

private[elasticsearch] case class HashedMedia(
  id: Id[HashedMedia],
  mimeType: MimeType,
  name: Option[String],
  hash: String,
  data: String) extends HasId[HashedMedia]

object MoneyMapping extends ESMapping[Money] {
  val currency = "currency" as String & Analyzed
  val amount = "amount" as Double & Analyzed
}

private[elasticsearch] object MediaMapping extends ESMapping[HashedMedia] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val mimeType = "mimeType" as String & Analyzed
  val name = "name" as String & Analyzed
  val hash = "hash" as String & NotAnalyzed
  val data = "data" as String & NotIndexed
}

class ESMediaDao(index: ESIndex)(implicit ec: ExecutionContext) extends Logging {

  private val hasher = Hashing.md5
  private val encoding = BaseEncoding.base64Url
  private implicit val hashMediaFormat = jsonFormat5(HashedMedia)
  private val dao = new ESRootDao[HashedMedia](index, ESType[HashedMedia]("media", MediaMapping))

  def retrieveAll: Future[List[Media]] =
    dao.retrieveAll map { medias =>
      medias.map(media =>
        media match {
          case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
            Media(Id(id.toString), mimeType, name, encoding.decode(data))
        })
    }

  def delete(id: Id[Media]): Future[Unit] =
    dao.delete(Id[HashedMedia](id.toString))

  def retrieve(id: Id[Media]): Future[Option[Media]] =
    dao.retrieve(Id[HashedMedia](id.toString)).map {
      _ map {
        case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
          Media(Id(id.toString), mimeType, name, encoding.decode(data))
      }
    }

  /**
   * The returned Media might have a different id.
   */
  def store(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Media] = {
    def hash = hasher.hashBytes(data).toString
    val stringData = encoding.encode(data)
    dao.search((MediaMapping.mimeType equ mimeType.value) or (MediaMapping.hash equ hash)) flatMap {
      _.find(m => m.data == stringData && m.name == name) match {
        case Some(Versioned(HashedMedia(id, mimeType, name, hash, data), version)) =>
          Future.successful(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
        case None =>
          val id = Id.generate[Media]
          dao.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData)).map(_ => Media(id, mimeType, name, data))
      }
    }
  }

  def importUrl(url: String): Future[Option[Media]] = {
    Future(readUrl(url)) flatMap {
      case Some(bytes) =>
        val name = url.substring(url.lastIndexOf('/') + 1)
        store(MimeTypes.fromResourceName(name), Some(name), bytes).map(Option(_))
      case None =>
        Future.successful(None)
    }
  }

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
