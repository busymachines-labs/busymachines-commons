package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.commons.elasticsearch.ESType
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.kentivo.mdm.domain.DomainJsonFormats.userFormat
import com.kentivo.mdm.domain.Party
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.kentivo.mdm.domain.User
import com.busymachines.commons.domain.Id

class UserDao(val parentDao : PartyDao)(implicit ec: ExecutionContext) extends ESNestedDao[Party, User]("user") {
  protected def findEntity(party: Party, id: Id[User]): Option[User] = 
    party.users.find(_.id == id)
    
  protected def createEntity(party: Party, entity: User): Party = 
    party.copy(users = party.users :+ entity)
    
  protected def modifyEntity(party: Party, id: Id[User], found: Found, modify: User => User): Party =
    party.copy(users = party.users.map {
      case user if user.id == id => found(modify(user))
      case user => user
    })
    
  protected def deleteEntity(party: Party, id: Id[User], found: Found): Party = 
    party.copy(users = party.users.filter {
      case user if user.id == id => found(user); false
      case user => true
    })
} 

