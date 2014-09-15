package com.busymachines.prefab.authentication.elasticsearch

import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.ESCollection
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.prefab.authentication.db.CredentialsDao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned

class ESCredentialsDao(index : ESIndex, typeName : String = "credentials")(implicit ec : ExecutionContext)
  extends CredentialsDao {
  
  private val collection = new ESCollection[Credentials](index, ESType("credentials", CredentialsMapping))
  
  def retrieve(id: Id[Credentials]): Future[Option[Credentials]] = 
    collection.retrieve(id.value)

  def findByLogin(login : String): Future[List[Credentials]] =
    collection.find(CredentialsMapping.passwordCredentials / PasswordCredentialsMapping.login equ login)

  def create(credentials: Credentials, refresh: Boolean): Future[Credentials] =
    collection.create(credentials, refresh)

  def modify(id: Id[Credentials], refresh: Boolean)(modify: Credentials => Credentials): Future[Credentials] =
    collection.modify(id.value, refresh)(modify)

  def delete(id: Id[Credentials], refresh: Boolean): Future[Unit] =
    collection.delete(id.value, refresh)

  def retrieveOrCreate(id: Id[Credentials], refresh: Boolean)(create: => Credentials): Future[Credentials] =
    collection.retrieveOrCreate(id.value, refresh)(create)

  def retrieveOrCreateAndModify(id: Id[Credentials], refresh: Boolean)(create: => Credentials)(modify: Credentials => Credentials): Future[Credentials] =
    collection.retrieveOrCreateAndModify(id.value, refresh)(create)(modify)
}
