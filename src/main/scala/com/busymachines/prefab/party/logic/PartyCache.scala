package com.busymachines.prefab.party.logic

import com.busymachines.prefab.party.db.PartyDao
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.busymachines.commons.dao.DaoCache
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.RelatedParty

class PartyCache(partyDao : PartyDao)(implicit ec: ExecutionContext) extends DaoCache[Party](partyDao){
 
  def relatedParties(partyId : Id[Party], exclude : Set[Id[Party]] = Set.empty) : List[RelatedParty] = {
    if (!exclude.contains(partyId)) 
      retrieve(partyId, 1.minute).map(_.entity) map { root =>
        val v2 = exclude ++ Set(partyId)
        root.relations ++ root.relations.flatMap(p => relatedParties(p.relatedParty, v2))
      } getOrElse(Nil)
    else Nil
  }
}