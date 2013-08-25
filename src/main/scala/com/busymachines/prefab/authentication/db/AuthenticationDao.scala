package com.busymachines.prefab.authentication.db

import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.domain.Id
import concurrent.Future
import com.busymachines.commons.dao.RootDao

trait AuthenticationDao extends RootDao[Authentication] {
  
  def retrieveAuthentication(id : Id[Authentication]) : Future[Option[Authentication]]
  
  def createAuthentication(authentication : Authentication) : Future[Unit]
}