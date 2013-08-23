package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.{ESType, ESRootDao, ESIndex}
import com.busymachines.prefab.authentication.model.Credentials
import spray.json.JsonFormat
import scala.concurrent.ExecutionContext

class ESCredentialsDao(index : ESIndex, indexType : String = "credentials")(implicit ec : ExecutionContext, credentialFormat : JsonFormat[Credentials])
  extends ESRootDao[Credentials](index, ESType("credentials", CredentialsMapping)) {

}
