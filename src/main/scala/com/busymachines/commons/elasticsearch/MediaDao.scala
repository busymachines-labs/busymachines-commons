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

private[elasticsearch] 
case class HashedMedia(
  id : Id[HashedMedia], 
  mimeType : String, 
  name : Option[String], 
  hash : String, 
  data : String
) extends HasId[HashedMedia]

private[elasticsearch] 
object MediaMapping extends Mapping[HashedMedia] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val mimeType = "mimeType" as String & Analyzed
  val name = "name" as String & Analyzed
  val hash = "hash" as String & NotAnalyzed
  val data = "data" as String & NotIndexed
}

class MediaDao(index: Index)(implicit ec: ExecutionContext) extends Logging {
  
  private val hasher = Hashing.md5
  private val encoding = BaseEncoding.base64Url
  private implicit val hashMediaFormat = jsonFormat5(HashedMedia)
  private val dao = new EsRootDao[HashedMedia](index, Type[HashedMedia]("media", MediaMapping))

  def retrieve(id : Id[Media]) : Future[Option[Media]] = 
    dao.retrieve(Id[HashedMedia](id.toString)).map { _ map {
      case Versioned(HashedMedia(id, mimeType, name, hash, data), version) =>
        Media(Id(id.toString), mimeType, name, encoding.decode(data))
    }
  }
  
  /**
   * The returned Media might have a different id.
   */
  def store(mimeType : String, name : Option[String], data : Array[Byte]) : Future[Media] = {
    def hash = hasher.hashBytes(data).toString
    val stringData = encoding.encode(data)
    dao.search(MediaMapping.mimeType === mimeType && MediaMapping.hash === hash) flatMap {
      _.find(m => m.data == stringData && m.name == name) match {
        case Some(Versioned(HashedMedia(id, mimeType, name, hash, data), version)) =>
          Future.successful(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
        case None =>
          val id = Id.generate[Media]
          dao.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData)).map(_ => Media(id, mimeType, name, data))
      }
    }
  }
  
  def importUrl(url : String) : Future[Option[Media]] = {
    Future(readUrl(url)) flatMap {
      case Some(bytes) => 
        val name = url.substring(url.lastIndexOf('/') + 1)
        store(MimeType.fromResourceName(name), Some(name), bytes).map(Option(_))
      case None => 
        Future.successful(None)
    }
  }
  
  def readUrl(url : String) : Option[Array[Byte]] = {
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
    }
    catch {
      case t : Throwable => debug(t); None
    }
  }
}