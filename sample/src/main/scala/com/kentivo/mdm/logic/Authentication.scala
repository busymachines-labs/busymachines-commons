package com.kentivo.mdm.logic

import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.User

case class AuthenticationData(partyId: Id[Party], userId: Id[User])
case class AuthenticationToken(token: String)

