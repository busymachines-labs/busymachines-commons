package com.busymachines.prefab.authentication.model

import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.Id

case class PasswordCredentials(id:Id[PasswordCredentials]=Id.generate[PasswordCredentials],salt:String,passwordHash:String) extends HasId[PasswordCredentials]