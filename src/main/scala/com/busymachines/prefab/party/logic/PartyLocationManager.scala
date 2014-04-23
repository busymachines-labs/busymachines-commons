package com.busymachines.prefab.party.logic

import com.busymachines.prefab.party.service.{SecurityContext, PartyLocationService}
import com.busymachines.prefab.party.domain.{Party, PartyLocation}
import com.busymachines.prefab.party.db._
import com.busymachines.commons.domain.Id
import scala.concurrent.{ExecutionContext, Future}
import com.busymachines.commons.dao.{Page, Versioned}
import com.busymachines.commons.EntityNotFoundException
import com.busymachines.commons.implicits._
import com.busymachines.commons.elasticsearch.ESSearchCriteria.And
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.elasticsearch.ESSearchCriteria.And
import com.busymachines.prefab.party.service.SecurityContext
import scala.Some
import com.busymachines.prefab.party.domain.PartyLocation
/**
 * Created by alex on 19.03.2014.
 */
class PartyLocationManager(locationDao:PartyLocationDao, partyDao:PartyDao, partyManager : PartyManager)(implicit ec:ExecutionContext) extends PartyLocationService{
  override def create(location:PartyLocation)(implicit sc:SecurityContext)=
    locationDao.create(sc.partyId, location, true)

  def get(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[PartyLocation]=
    locationDao.retrieve(locationId).map{
      case Some(loc)=>loc.entity
      case None=>throw new EntityNotFoundException(locationId.toString,"location")
    }

  def findLocations(
                     partyIds: Option[List[Id[Party]]],
                     swLat: Option[Double],
                     swLon: Option[Double],
                     neLat: Option[Double],
                     neLon: Option[Double],
                     limit: Option[Int],
                     offset: Option[Int],
                     costCenters: List[String])(implicit securityContext:SecurityContext): List[PartyLocation] = {

    var Ids:List[Id[Party]] = partyIds.getOrElse(partyManager.listChildPartiesIds.await ::: List(securityContext.partyId))

    //TODO search by costcenters
    //      costCenters  match{ case List(a,_*) => PartyMapping}

    partyDao.search(
      And(
        Some(PartyMapping.id in Ids) ::
          (swLat.map(sw => PartyMapping.locations / PartyLocationMapping.address / AddressMapping.geoLocation / GeoPointMapping.lat gte sw)) ::
          (swLon.map(sw => PartyMapping.locations / PartyLocationMapping.address / AddressMapping.geoLocation / GeoPointMapping.lon gte sw)) ::
          (neLat map(ne => PartyMapping.locations / PartyLocationMapping.address / AddressMapping.geoLocation / GeoPointMapping.lat lte ne)) ::
          (neLon map(ne => PartyMapping.locations / PartyLocationMapping.address / AddressMapping.geoLocation / GeoPointMapping.lon lte ne)) ::
          Nil flatten),
      Page(offset,limit)).map(_.result.map(_.entity.map(_.locations)) flatten).await.flatten

  }

  def delete(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[Unit]=
    locationDao.delete(locationId)
}
