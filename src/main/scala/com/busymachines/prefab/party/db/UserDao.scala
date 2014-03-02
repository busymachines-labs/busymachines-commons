package com.busymachines.prefab.party.db

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.Versioned
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.implicits._

class UserDao(val parentDao : PartyDao)(implicit ec: ExecutionContext) extends ESNestedDao[Party, User]("user") {
    
  def retrieveParent(id : Id[User]) : Future[Option[Versioned[Party]]] = 
    parentDao.findByUserId(id)
  
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

