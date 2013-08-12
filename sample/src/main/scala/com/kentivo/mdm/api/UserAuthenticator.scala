package com.kentivo.mdm.api

import com.busymachines.commons.http.AbstractAuthenticator
import com.kentivo.mdm.domain.User
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.logic.AuthenticationData

class UserAuthenticator(implicit executionContext: ExecutionContext) extends AbstractAuthenticator[AuthenticationData] 