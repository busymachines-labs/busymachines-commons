package com.busymachines.prefab.authentication.model

import com.busymachines.commons.implicits._
import scala.util.Random
import com.busymachines.commons.domain.{HasId, Id}
import org.joda.time.DateTime

case class Credentials(
  id: Id[Credentials] = Id.generate,
  passwordCredentials: List[PasswordCredentials] = Nil,
  passwordHints : List[PasswordHint] = Nil) extends HasId[Credentials]

case class PasswordHint(
	securityQuestion:Option[String] = None,
	securityAnswer:Option[String] = None
)