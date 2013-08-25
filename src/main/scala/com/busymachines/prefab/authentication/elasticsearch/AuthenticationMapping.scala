package com.busymachines.prefab.authentication.elasticsearch

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.authentication.model.Authentication
import spray.json.JsObject

object AuthenticationMapping extends ESMapping[Authentication] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val principal = "principal" as String & NotAnalyzed
  val expirationTime = "expirationTime" as Date & NotAnalyzed
}