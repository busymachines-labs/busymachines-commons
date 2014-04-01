package com.busymachines.prefab.party.logic

import com.busymachines.prefab.party.service.{SecurityContext, PartyLocationService}
import com.busymachines.prefab.party.domain.PartyLocation
import com.busymachines.prefab.party.db.PartyLocationDao
import com.busymachines.commons.domain.Id
import scala.concurrent.{ExecutionContext, Future}
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.EntityNotFoundException

/**
 * Created by alex on 19.03.2014.
 */
class PartyLocationManager(locationDao:PartyLocationDao)(implicit ec:ExecutionContext) extends PartyLocationService{
  override def create(location:PartyLocation)(implicit sc:SecurityContext)=
    locationDao.create(sc.partyId, location, true)

  def get(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[PartyLocation]=
    locationDao.retrieve(locationId).map{
      case Some(loc)=>loc.entity
      case None=>throw new EntityNotFoundException(locationId.toString,"location")
    }

  def delete(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[Unit]=
    locationDao.delete(locationId)
}
