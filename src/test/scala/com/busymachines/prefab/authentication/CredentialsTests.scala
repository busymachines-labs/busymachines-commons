package com.busymachines.prefab.authentication

import org.scalatest.FlatSpec
import com.busymachines.commons.testing.EmptyESTestIndex
import com.busymachines.commons.event.DoNothingEventSystem
import scala.concurrent.ExecutionContext.Implicits.global
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.prefab.authentication.model.PasswordCredentials
import com.busymachines.commons.Logging
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.prefab.authentication.Implicits._
import com.busymachines.commons.Implicits.richFuture
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CredentialsTests extends FlatSpec with Logging {

  val esIndex = EmptyESTestIndex(getClass)
  val dao = new ESCredentialsDao(esIndex)

  "CredentialsDao" should "create & retrieve" in {
    val credentials = Credentials(passwordCredentials = List(PasswordCredentials("login", "changeme")))
    dao.create(credentials).await
    assert(dao.retrieve(credentials.id).await.get.id === credentials.id)
    assert(dao.retrieve(credentials.id).await.get.passwordCredentials.head.hasPassword("changeme"))

  }

}