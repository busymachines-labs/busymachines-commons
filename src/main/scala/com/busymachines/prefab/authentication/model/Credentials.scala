package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id

case class Credentials(id:Id[Credentials]=Id.generate[Credentials],passwordCredentials:Option[PasswordCredentials]=None) extends HasId[Credentials]