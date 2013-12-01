package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.authentication.model.Authentication
import spray.json.JsObject
import scala.concurrent.duration._

object AuthenticationMapping extends ESMapping[Authentication] {
  ttl = Some(7.days)
  val id = "id" -> "_id" as String & NotAnalyzed
  val principal = "principal" as String & NotAnalyzed
  val expirationTime = "expirationTime" as Date & NotAnalyzed
}