package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.{Id, HasId}
import org.joda.time.DateTime
import scala.concurrent.duration.Deadline
import spray.json.JsValue

/**
 * An authentication is a temporary object that is used to store information 
 * about a user or other entity that is currently authenticated by the system.
 * @param id The id serves as an authentication token that is communicated with clients.
 * @param principal The principal identifies the user (or other entity) that is authenticated. When an 
 * authentication is retrieved, the principal is used to fetch the information needed to
 * instantiate a local security context.
 * @param expirationTime Time until which the authentication is valid. 
 */
case class Authentication(id: Id[Authentication], principal : JsValue, expirationTime : DateTime)
  extends HasId[Authentication]