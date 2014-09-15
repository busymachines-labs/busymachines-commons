package com.busymachines.prefab.authentication.db

import com.busymachines.commons.dao.Dao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned

trait CredentialsDao {
  
  def retrieve(id: Id[Credentials]): Future[Option[Credentials]]
  def findByLogin(login : String) : Future[List[Credentials]]
  def create(credentials: Credentials, refresh: Boolean = true): Future[Credentials]
  def modify(id: Id[Credentials], refresh: Boolean = true)(modify: Credentials => Credentials): Future[Credentials]
  def delete(id: Id[Credentials], refresh: Boolean = true): Future[Unit]
  def retrieveOrCreate(id: Id[Credentials], refresh: Boolean = true)(create: => Credentials): Future[Credentials]
  def retrieveOrCreateAndModify(id: Id[Credentials], refresh: Boolean = true)(create: => Credentials)(modify: Credentials => Credentials): Future[Credentials]
}