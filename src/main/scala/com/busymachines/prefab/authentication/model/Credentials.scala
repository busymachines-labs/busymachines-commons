package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.{HasId, Id}

case class Credentials(
  id: Id[Credentials] = Id.generate,
  passwordCredentials: List[PasswordCredentials] = Nil,
  passwordHints : List[PasswordHint] = Nil) extends HasId[Credentials]

case class PasswordHint(
	securityQuestion:Option[String] = None,
	securityAnswer:Option[String] = None
)