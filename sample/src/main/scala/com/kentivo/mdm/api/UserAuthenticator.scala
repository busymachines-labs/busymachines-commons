package com.kentivo.mdm.api

import com.busymachines.commons.http.AbstractAuthenticator
import com.kentivo.mdm.domain.User
import scala.concurrent.ExecutionContext

class UserAuthenticator(implicit executionContext: ExecutionContext) extends AbstractAuthenticator[User] {

}