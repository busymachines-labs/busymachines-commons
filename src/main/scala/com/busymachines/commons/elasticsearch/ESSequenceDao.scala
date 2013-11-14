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
import com.busymachines.commons.domain.Sequence
import org.elasticsearch.index.engine.VersionConflictEngineException
import scala.annotation.tailrec
import com.busymachines.commons.implicits._
import com.busymachines.commons.dao.Versioned.toEntity

private[elasticsearch] object SequenceMapping extends ESMapping[Sequence] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val value = "value" as Long
}

class ESSequenceDao(index: ESIndex)(implicit ec: ExecutionContext) extends Logging {

  private val hasher = Hashing.md5
  private val encoding = BaseEncoding.base64Url
  private implicit val sequenceFormat = jsonFormat3(Sequence)
  private val dao = new ESRootDao[Sequence](index, ESType[Sequence]("sequence", SequenceMapping))

  def retrieveAll: Future[List[Sequence]] =
    dao.retrieveAll map { medias =>
      medias.map(sequence => sequence.entity )
  }

  def delete(id: Id[Sequence]): Future[Unit] =
    dao.delete(Id[Sequence](id.toString))
    
  def retrieve(id: Id[Sequence]): Future[Option[Sequence]] =
    dao.retrieve(Id[Sequence](id.toString)).map {
      _ map {
        case s => s.entity
      }
    }
    
  def create(sequence:Sequence):Future[Sequence] = 
    dao.create(sequence) map {
      case s => s.entity
  }

  def next(sequence:Sequence,incrementValue:Long=1): Future[Long] = {
    retry(increment(sequence,incrementValue), 1000, 0).get
  }

  @tailrec 
  private def retry(future: Option[Future[Long]], maxAttempts: Int, attempt: Int): Option[Future[Long]] = {
    try {
      future.map { s => s
      }
    } catch {
      case e: VersionConflictEngineException =>
        if (attempt <= maxAttempts) retry(future, maxAttempts, attempt + 1)
        else None
    }
  }

  private def increment(sequence:Sequence,incrementValue:Long=1): Option[Future[Long]] = 
    dao.modify(sequence.id) { entity =>
      entity.copy(value = entity.value + incrementValue)
    } map {
      case Versioned(Sequence(id,name, value), version) => value
    }
  
}
