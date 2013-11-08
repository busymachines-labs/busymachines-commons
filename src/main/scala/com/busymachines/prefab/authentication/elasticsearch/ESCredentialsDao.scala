package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.{ESType, ESRootDao, ESIndex}
import com.busymachines.prefab.authentication.model.Credentials
import spray.json.JsonFormat
import scala.concurrent.ExecutionContext
import com.busymachines.commons.dao.RootDao
import com.busymachines.prefab.authentication.db.CredentialsDao
import com.busymachines.prefab.authentication.model.SecurityJsonFormats._
import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned

class ESCredentialsDao(index : ESIndex, indexType : String = "credentials")(implicit ec : ExecutionContext)
  extends ESRootDao[Credentials](index, ESType("credentials", CredentialsMapping)) with CredentialsDao {

  def retrieveWithPassword(id : Id[Credentials], password : String) : Future[Option[Credentials]] = {
    retrieve(id) map {
      credentials : Option[Versioned[Credentials]] =>
        credentials.map(_.entity).filter(_.passwordCredentials.exists(_.hasPassword(password))) 
    }
  }

}
