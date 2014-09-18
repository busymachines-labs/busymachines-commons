package com.busymachines.prefab.authentication

import org.scalatest.FlatSpec
import com.busymachines.commons.testing.EmptyESTestIndex
import scala.concurrent.ExecutionContext.Implicits.global
import com.busymachines.prefab.authentication.model.{HashFunctions, Credentials, PasswordCredentials}
import com.busymachines.commons.logging.Logging
import com.busymachines.prefab.authentication.elasticsearch.ESCredentialsDao
import com.busymachines.commons.Implicits.richFuture
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class CredentialsTests extends FlatSpec with Logging {

  val esIndex = EmptyESTestIndex(getClass)
  val dao = new ESCredentialsDao(esIndex)

  "CredentialsDao" should "create & retrieve" in {
    val credentials = Credentials(passwordCredentials = List(PasswordCredentials("login", "changeme", HashFunctions.sha256)))
    dao.create(credentials).await
    assert(dao.retrieve(credentials.id).await.get.id === credentials.id)
    assert(dao.retrieve(credentials.id).await.get.passwordCredentials.head.hasPassword("changeme", HashFunctions.sha256))

  }

}