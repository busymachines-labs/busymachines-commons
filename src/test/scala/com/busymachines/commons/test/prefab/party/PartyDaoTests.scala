package com.busymachines.commons.test.prefab.party

import org.scalatest.FlatSpec
import com.busymachines.commons.testing.EmptyESTestIndex
import com.busymachines.commons.event.DoNothingEventSystem
import scala.concurrent.ExecutionContext.Implicits.global
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.prefab.authentication.implicits._
import com.busymachines.commons.implicits.richFuture
import com.busymachines.prefab.party.db.PartyDao
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.Tenant
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.domain.Address
import com.busymachines.prefab.party.domain.Party
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PartyDaoTests extends FlatSpec with Logging {

  val esIndex = new EmptyESTestIndex(getClass, new DoNothingEventSystem)
  val dao = new PartyDao(esIndex)
  
  "PartyDao" should "create & retrieve" in {
    val tenantId = Id.static[Tenant]("Tenant1")

    val user1 = User(
      id = Id.static[User]("user1"),
      credentials = Id.generate[Credentials],
      firstName = Some("Joe"),
      lastName = Some("Doe"),
      addresses = Address(street = Some("Street 1")) :: Nil)

    val party1 = Party(
      tenant = Some(tenantId),
      id = Id.static[Party]("party1"),
      users = user1 :: Nil)

    dao.create(party1).await  
    assert (dao.findUserByCredentialsId(user1.credentials).await !== None)
  }
  
}