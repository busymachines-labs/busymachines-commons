package com.kentivo.mdm.api.v1.model

case class AuthenticationResponse(userId: String)
case class AuthenticationRequest(loginName: String, password: String)
