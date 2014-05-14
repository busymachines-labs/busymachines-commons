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
import com.busymachines.commons.Implicits.richFuture

object PartyFixture extends PartyFixture 

trait PartyFixture {

  val testTenantId = Id.static[Tenant]("test-tenant-1")
  val testParty1Id = Id.static[Party]("test-party-1")
  val testUser1Id = Id.static[User]("test-user-1")
  val testUser1CredentialsId = Id.static[Credentials]("test-user-1-credentials")

  val testUser1Username = "user1@test.com"
  val testUser1Password = "test"

  //TODO after full migration, use custom UIDs for party and user ID
  //These are used for machinexs project, since ES users and parties are mapped 1 - 1 with SQL parties and users
  val testTenant2Id = Id.static[Tenant]("test-tenant-1")
  val testParty2Id = Id.static[Party]("-1")
  val testUser2Id = Id.static[User]("14")
  val testUser2CredentialsId = Id.static[Credentials]("test-user-2-credentials")

  val testUser2Username = "user2@test.com"
  val testUser2Password = "test"
  
  private[party] def create(partyDao : PartyDao, credentialsDao : ESCredentialsDao) {
    if (CommonConfig.devmode) {
      createDevMode(partyDao, credentialsDao)
    } else {
      partyDao.delete(testParty1Id).await
      credentialsDao.delete(testUser1CredentialsId).await
    }
  }
  
  def createDevMode(partyDao : PartyDao, credentialsDao : ESCredentialsDao) {
    val user1 = User(
      id = testUser1Id,
      credentials = testUser1CredentialsId,
      firstName = Some("John"),
      lastName = Some("Doe"),
      addresses = Address(street = Some("Street 1")) :: Nil)

    val user2 = User(
      id = testUser2Id,
      credentials = testUser2CredentialsId,
      firstName = Some("John 2"),
      lastName = Some("Doe"),
      addresses = Address(street = Some("Street 2")) :: Nil)

    (partyDao.getOrCreateAndModify(testParty1Id)(Party(testParty1Id)) { party =>
      party.copy(tenant = Some(testTenantId), users = user1 :: Nil, company = Some(Company("Test Company")),
          addresses = Address(street = Some("Korenmolen"), houseNumber = Some("3"), postalCode = Some("1541RW"), city = Some("Koog aan de Zaan")) :: Nil)
    }).await
    
    (credentialsDao.getOrCreateAndModify(testUser1CredentialsId)(Credentials(testUser1CredentialsId)) { credentials =>
      credentials.copy(passwordCredentials = PasswordCredentials(testUser1Username, testUser1Password) :: Nil)
    }).await

    (partyDao.getOrCreateAndModify(testParty2Id)(Party(testParty2Id)) { party =>
      party.copy(tenant = Some(testTenant2Id), users = user2 :: Nil, company = Some(Company("Test 2 Company")),
        addresses = Address(street = Some("Korenmolen"), houseNumber = Some("3"), postalCode = Some("1541RW"), city = Some("Koog aan de Zaan")) :: Nil)
    }).await

    (credentialsDao.getOrCreateAndModify(testUser2CredentialsId)(Credentials(testUser2CredentialsId)) { credentials =>
      credentials.copy(passwordCredentials = PasswordCredentials(testUser2Username, testUser2Password) :: Nil)
    }).await
  }
}