package com.busymachines.commons.test.prefab.authentication

import org.scalatest.FlatSpec
import com.busymachines.commons.testing.EmptyESTestIndex
import com.busymachines.commons.event.DoNothingEventSystem
import scala.concurrent.ExecutionContext.Implicits.global
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.prefab.authentication.model.SecurityJsonFormats._
import com.busymachines.commons.implicits.richFuture

class CredentialsTests extends FlatSpec with Logging {

  val esIndex = new EmptyESTestIndex(getClass, new DoNothingEventSystem)
  val dao = new ESCredentialsDao(esIndex)

  "CredentialsDao" should "create & retrieve" in {
    val credentials = Credentials(passwordCredentials=Some(PasswordCredentials(salt="",passwordHash="Hello World")))
    dao.create(credentials).await
    assert(dao.retrieve(credentials.id).await.get.id === credentials.id)
    assert(dao.retrieve(credentials.id).await.get.passwordCredentials.get.passwordHash === credentials.passwordCredentials.get.passwordHash)

  }
  
}