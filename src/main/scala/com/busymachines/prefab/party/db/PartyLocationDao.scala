package com.busymachines.prefab.party.db

import scala.concurrent.{Future, ExecutionContext}
import com.busymachines.commons.elasticsearch.ESNestedDao
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.{PartyLocation, Party, User}
import com.busymachines.commons.dao.{Page, Versioned}
import com.busymachines.prefab.party.Implicits._
import com.busymachines.prefab.party.service.SecurityContext
import com.busymachines.commons.EntityNotFoundException

class PartyLocationDao(val parentDao : PartyDao)(implicit ec: ExecutionContext) extends ESNestedDao[Party, PartyLocation]("location") {
  def retrieveParent(id : Id[PartyLocation]) : Future[Option[Versioned[Party]]] = parentDao.findByLocationId(id)

  def findEntity(party: Party, id: Id[PartyLocation]): Option[PartyLocation] = party.locations.find(_.id == id)

  def createEntity(parent: Party, entity: PartyLocation): Party = parent.copy(locations = parent.locations :+ entity)

  def findLocation(id:Id[PartyLocation])=parentDao.searchSingle(PartyMapping.locations / PartyLocationMapping.id equ id).map {
    case Some(party) => party.entity.locations.find(_.id == id)
    case None =>throw new EntityNotFoundException(id.toString, "location")
  }

  def modifyEntity(party: Party, id: Id[PartyLocation], found: Found, modify: PartyLocation => PartyLocation): Party =
    party.copy(locations = party.locations.map {
      case location if location.id == id => found(modify(location))
      case location => location
    })

  def deleteEntity(party: Party, id: Id[PartyLocation], found: Found): Party =
    party.copy(locations = party.locations.filter {
      case location if location.id == id => found(location); false
      case location => true
    })
} 

