package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.commons.elasticsearch.{ESType, ESIndex, ESRootDao}
import com.busymachines.prefab.authentication.Implicits._
import spray.json.JsonFormat
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.busymachines.prefab.authentication.db.AuthenticationDao
import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import org.joda.time.DateTime
import org.elasticsearch.common.joda.time.Duration

class ESAuthenticationDao(index : ESIndex, indexType : String = "authentication")(implicit ec : ExecutionContext)
  extends ESRootDao[Authentication](index, ESType("authentication", AuthenticationMapping)) 
  with AuthenticationDao {
  
  def retrieveAuthentication(id : Id[Authentication]) : Future[Option[Authentication]] = 
    retrieve(id)
  
  def createAuthentication(authentication : Authentication) : Future[Unit] = {
    val millis = authentication.expirationTime.getMillis - DateTime.now.getMillis
    // make sure the document outlives the expiration of the document
    val ttl = if (millis > 0) Some((millis * 1.3).milliseconds) else None
    create(authentication, true, ttl) map {_ => }
  }
}
