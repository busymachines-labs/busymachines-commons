package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.{Id, HasId}
import org.joda.time.DateTime
import scala.concurrent.duration.Deadline
import spray.json.JsValue

case class Authentication(id: Id[Authentication], principal : JsValue, expirationTime : DateTime)
  extends HasId[Authentication]