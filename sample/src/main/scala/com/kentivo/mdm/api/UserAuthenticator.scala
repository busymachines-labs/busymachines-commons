package com.kentivo.mdm.api

import scala.concurrent.ExecutionContext

import com.busymachines.commons.http.CommonAuthenticator
import com.kentivo.mdm.logic.AuthenticationData
import com.kentivo.mdm.logic.PartyService

class UserAuthenticator(partyService: PartyService)(implicit executionContext: ExecutionContext) extends CommonAuthenticator[AuthenticationData] {

  def authenticateUser(email: String, password: String) =
    partyService.authenticate(email, password) map {
      case Some((party, user, login)) =>
      case None =>
    }
} 