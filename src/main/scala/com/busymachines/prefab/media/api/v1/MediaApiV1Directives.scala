package com.busymachines.prefab.media.api.v1

import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.spray.AuthenticationDirectives
import com.busymachines.prefab.media.api.v1.model.MediaApiV1JsonFormats
import spray.httpx.SprayJsonSupport
import spray.routing.Directives

trait MediaApiV1Directives extends Directives with Logging with SprayJsonSupport with MediaApiV1JsonFormats with AuthenticationDirectives 
