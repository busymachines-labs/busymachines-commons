package com.busymachines.prefab.party.api.v1.model

import com.busymachines.prefab.party.domain.PartyDomainJsonFormats

object PartyApiV1JsonFormats extends PartyApiV1JsonFormats

trait PartyApiV1JsonFormats extends PartyDomainJsonFormats {
  implicit val authenticationRequestFormat = jsonFormat2(AuthenticationRequest)
  implicit val authenticationResponseFormat = jsonFormat3(AuthenticationResponse)
}


