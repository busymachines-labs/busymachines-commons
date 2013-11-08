package com.busymachines.prefab.authentication.db

import com.busymachines.commons.dao.Dao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.domain.Id
import scala.concurrent.Future

trait CredentialsDao extends RootDao[Credentials] {
  def retrieveWithPassword(ids : Id[Credentials], password : String) : Future[Option[Credentials]]
}