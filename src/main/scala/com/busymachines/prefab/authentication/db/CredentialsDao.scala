package com.busymachines.prefab.authentication.db

import com.busymachines.commons.dao.Dao
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.dao.RootDao

trait CredentialsDao extends RootDao[Credentials]