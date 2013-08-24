package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import spray.json.DefaultJsonProtocol._
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESProperty.toPath
import com.busymachines.commons.elasticsearch.ESRootDao
import com.busymachines.commons.elasticsearch.ESType
import com.kentivo.mdm.domain.DomainJsonFormats.loginFormat
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.Login
import scala.concurrent.Future

class LoginDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESRootDao[Login](index, ESType("login", LoginMapping)) {
  
  def findByEmail(email : String) : Future[Option[Versioned[Login]]] = 
    searchSingle(LoginMapping.email === email)
  
} 

