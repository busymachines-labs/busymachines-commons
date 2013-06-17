package com.kentivo.mdm.api

import spray.json.DefaultJsonProtocol
import com.kentivo.mdm.api.v1.AuthenticationUser

trait ApiJsonFormats extends DefaultJsonProtocol {
  implicit val authenticationUser = jsonFormat2(AuthenticationUser)
}
