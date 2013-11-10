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
import com.busymachines.prefab.authentication.model.Credentials

class ESCredentialsDao(index : ESIndex, indexType : String = "credentials")(implicit ec : ExecutionContext)
  extends ESRootDao[Credentials](index, ESType("credentials", CredentialsMapping)) with CredentialsDao {
  
  def findByLogin(login : String) = 
    search(CredentialsMapping.passwordCredentials / PasswordCredentialsMapping.login equ login)

}
