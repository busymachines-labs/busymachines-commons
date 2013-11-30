package com.busymachines.prefab.party.api.v1.model

case class AuthenticationResponse(authToken: String, userId : String, partyId : String)
case class AuthenticationRequest(loginName: String, password: String)
