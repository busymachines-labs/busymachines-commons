package com.busymachines.prefab.authentication.db

import com.busymachines.commons.dao.Dao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.dao.RootDao
import com.busymachines.commons.domain.Id
import scala.concurrent.Future
import com.busymachines.commons.dao.Versioned

trait CredentialsDao extends RootDao[Credentials] {
  def findByLogin(login : String) : Future[List[Versioned[Credentials]]]
}