package com.busymachines.prefab.party.api.v1.model

case class AuthenticationResponse(authToken: String, userId : String, partyId : String,permissions:Set[String]=Set.empty)
case class AuthenticationRequest(loginName: String, password: String, party:Option[String]=None)
