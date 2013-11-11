package com.busymachines.prefab.party.api.v1

import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives
import com.busymachines.prefab.party.api.v1.model.PartyApiJsonFormats

import spray.httpx.SprayJsonSupport
import spray.routing.Directives

trait PartyApiDirectives extends Directives with Logging with SprayJsonSupport with PartyApiJsonFormats with AuthenticationDirectives 
