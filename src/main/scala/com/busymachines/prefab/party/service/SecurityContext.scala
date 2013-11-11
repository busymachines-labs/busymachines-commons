package com.busymachines.prefab.party.service

import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.prefab.authentication.logic.PrefabSecurityContext
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.Tenant
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.domain.Permission

case class SecurityContext(
  tenantId: Id[Tenant],
  partyId: Id[Party],
  userId: Id[User],
  user: String,
  authenticationId: Id[Authentication],
  permissions: Set[Permission] = Set.empty) extends PrefabSecurityContext[Permission] {
  
  def principalDescription = user
}
