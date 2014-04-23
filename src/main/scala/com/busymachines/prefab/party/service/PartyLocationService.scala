package com.busymachines.prefab.party.service

import com.busymachines.prefab.party.domain.{Party, PartyLocation}
import com.busymachines.commons.dao.Versioned
import scala.concurrent.Future
import com.busymachines.commons.domain.Id

/**
 * Created by alex on 19.03.2014.
 */
trait PartyLocationService {
  def create(location:PartyLocation)(implicit sc:SecurityContext):Future[Versioned[PartyLocation]]
  def get(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[PartyLocation]
  def delete(locationId:Id[PartyLocation])(implicit sc:SecurityContext):Future[Unit]
  def findLocations(
                     partyIds: Option[List[Id[Party]]],
                     swLat: Option[Double],
                     swLon: Option[Double],
                     neLat: Option[Double],
                     neLon: Option[Double],
                     limit: Option[Int],
                     offset: Option[Int],
                     costCenters: List[String])(implicit securityContext:SecurityContext): List[PartyLocation]
}

