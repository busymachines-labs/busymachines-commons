package com.busymachines.prefab.authentication.db

import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.Id
import concurrent.Future

trait AuthenticationDao {
  
  def retrieveAuthentication(id : Id[Authentication]) : Future[Option[Authentication]]
  
  def createAuthentication(authentication : Authentication) : Future[Nothing]
}