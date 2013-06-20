//package com.kentivo.mdm.db
//
//import scala.annotation.implicitNotFound
//import scala.concurrent.ExecutionContext
//import scala.concurrent.Future
//import org.elasticsearch.index.query.FilterBuilders.termFilter
//import com.google.common.hash.Hashing
//import com.google.common.io.BaseEncoding
//import com.kentivo.mdm.commons.ESSourceProvider
//import com.kentivo.mdm.commons.HasId
//import com.busymachines.commons
//import com.kentivo.mdm.commons.implicits.toOption
//import com.kentivo.mdm.domain.DomainJsonFormats
//import com.kentivo.mdm.domain.Media
//import com.kentivo.mdm.domain.MimeType
//import java.io.BufferedInputStream
//import java.net.URL
//
//private[db] case class HashedMedia(id : Id[HashedMedia], mimeType : String, name : Option[String], hash : String, data : String) extends HasId[HashedMedia]
//
//class MediaDao(provider: ESSourceProvider)(implicit ec: ExecutionContext) extends AbstractDao[HashedMedia](provider.MEDIA) with DomainJsonFormats {
//
//  private val hasher = Hashing.md5()
//  private val encoding = BaseEncoding.base64Url
//  private implicit val hashMediaFormat = jsonFormat5(HashedMedia)
//
//  def get(id : Id[Media]) : Future[Option[Media]] = 
//    super.get(Id[HashedMedia](id.toString)).map { _ map {
//      case HashedMedia(id, mimeType, name, hash, data) =>
//        Media(Id(id.toString), mimeType, name, encoding.decode(data))
//    }
//  }
//  
//  /**
//   * The returned Media might have a different id.
//   */
//  def store(mimeType : String, name : Option[String], data : Array[Byte]) : Future[Media] = {
//    def hash = hasher.hashBytes(data).toString
//    val stringData = encoding.encode(data)
//    super.find(termFilter("mimeType", mimeType), termFilter("hash", hash)) flatMap {
//      _.find(m => m.data == stringData && m.name == name) match {
//        case Some(HashedMedia(id, mimeType, name, hash, data)) =>
//          Future.successful(Media(Id(id.toString), mimeType, name, encoding.decode(data)))
//        case None =>
//          val id = Id.generate[Media]
//          super.create(HashedMedia(Id(id.toString), mimeType, name, hash, stringData)).map(_ => Media(id, mimeType, name, data))
//      }
//    }
//  }
//  
//  def importUrl(url : String) : Future[Option[Media]] = {
//    Future(readUrl(url)) flatMap {
//      case Some(bytes) => 
//        val name = url.substring(url.lastIndexOf('/') + 1)
//        store(MimeType.fromResourceName(name), name, bytes).map(Option(_))
//      case None => Future.successful(None)
//    }
//  }
//  
//  def readUrl(url : String) : Option[Array[Byte]] = {
//    try {
//      if (url.toString.isEmpty()) None
//      else {
//        val bis = new BufferedInputStream(new URL(url.toString).openStream())
//        val bytes = Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
//        bis.close
//        Some(bytes)
//      }
//    }
//    catch {
//      case t : Throwable => println(t); None
//    }
//  }
//}