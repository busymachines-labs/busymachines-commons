package com.busymachines.prefab.authentication.model

import com.busymachines.commons.implicits._
import scala.util.Random
import com.busymachines.commons.domain.{HasId, Id}
import org.joda.time.DateTime
import com.busymachines.commons.http.AuthenticationToken

case class Credentials(
  id: Id[Credentials] = Id.generate,
  passwordCredentials: Option[PasswordCredentials] = None) extends HasId[Credentials]
