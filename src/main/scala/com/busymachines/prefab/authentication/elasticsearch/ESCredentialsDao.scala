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

  def retrieveWithPassword(ids : Seq[Id[Credentials]], password : String) : Future[Option[Credentials]] = {
    retrieve(ids) map {
      credentials : List[Versioned[Credentials]] =>
        credentials.find(_.entity.passwordCredentials.map(_.hasPassword(password)).getOrElse(false)) map {
          case Versioned(credentials, _) => credentials
        }
    }
  }

}
