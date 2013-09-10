package com.kentivo.mdm.api.v1.model

import spray.httpx.SprayJsonSupport
import com.kentivo.mdm.domain.DomainJsonFormats

object ApiV1JsonFormats extends ApiV1JsonFormats

trait ApiV1JsonFormats extends SprayJsonSupport with DomainJsonFormats {
  implicit val authenticationRequestFormat = jsonFormat2(AuthenticationRequest)
  implicit val authenticationResponseFormat = jsonFormat1(AuthenticationResponse)
}


