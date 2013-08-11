package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.kentivo.mdm.domain.DomainJsonFormats.userFormat
import com.kentivo.mdm.domain.Party
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.kentivo.mdm.domain.User

class UserDao(index : ESIndex)(implicit ec: ExecutionContext) extends ESNestedDao[Party, User]("user") 

