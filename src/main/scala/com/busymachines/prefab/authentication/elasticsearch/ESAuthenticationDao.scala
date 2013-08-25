package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.commons.elasticsearch.{ESType, ESIndex, ESRootDao}
import com.busymachines.prefab.authentication.model.SecurityJsonFormats._
import spray.json.JsonFormat
import scala.concurrent.ExecutionContext
import com.busymachines.prefab.authentication.db.AuthenticationDao
import com.busymachines.commons.domain.Id
import scala.concurrent.Future

class ESAuthenticationDao(index : ESIndex, indexType : String = "authentication")(implicit ec : ExecutionContext)
  extends ESRootDao[Authentication](index, ESType("authentication", AuthenticationMapping)) 
  with AuthenticationDao {
  
  def retrieveAuthentication(id : Id[Authentication]) : Future[Option[Authentication]] = 
    retrieve(id).map(_.map(_.entity.asInstanceOf[Authentication]))
  
  def createAuthentication(authentication : Authentication) : Future[Unit] = 
    create(authentication) map {_ => }

}
