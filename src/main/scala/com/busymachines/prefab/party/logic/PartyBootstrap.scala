package com.busymachines.prefab.party.logic

import com.busymachines.commons.CommonConfig
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.busymachines.prefab.party.db.PartyDao
import com.busymachines.prefab.party.db.UserDao
import com.busymachines.prefab.party.domain.Address
import com.busymachines.prefab.party.domain.Company
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.Tenant
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.service.PartyService

class PartyBootstrap(partyDao : PartyDao, credentialsDao : ESCredentialsDao) {

  def bootstrap {
    if (CommonConfig.devmode) {
      
      val tenantId = Id.static[Tenant]("test-tenant-1")
      val party1Id = Id.static[Party]("test-party-1")
      
      val user1 = User(
        id = Id.static[User]("user1"),
        firstName = Some("John"),
        lastName = Some("Doe"),
        addresses = Address(street = Some("Street 1")) :: Nil)
  
      partyDao.getOrCreateAndModify(party1Id)(Party(party1Id, tenantId)) { party =>
        party.copy(tenant = tenantId, users = user1 :: Nil, company = Some(Company("Test Company")))
      }
      
      credentialsDao.getOrCreateAndModify(user1.credentials)(Credentials(user1.credentials)) { credentials =>
        credentials.copy(passwordCredentials = PasswordCredentials("user1@test.com", "test") :: Nil)
      }
    }
  }
}