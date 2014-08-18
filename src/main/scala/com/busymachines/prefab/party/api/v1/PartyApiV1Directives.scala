package com.busymachines.prefab.party.api.v1

import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives

import spray.httpx.SprayJsonSupport
import spray.routing.Directives

trait PartyApiV1Directives extends Directives with AuthenticationDirectives with SprayJsonSupport
