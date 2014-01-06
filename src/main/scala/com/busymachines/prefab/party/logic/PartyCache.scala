package com.busymachines.prefab.party.logic

import com.busymachines.prefab.party.db.PartyDao
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.busymachines.commons.dao.DaoCache
import com.busymachines.commons.implicits._
import com.busymachines.prefab.party.domain.Party
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.party.domain.RelatedParty
import scala.collection.concurrent.TrieMap
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.service.SecurityContext

class PartyCache(partyDao : PartyDao, userAuthenticator : UserAuthenticator)(implicit ec: ExecutionContext) extends DaoCache[Party](partyDao, true) {
 
  private val relatedParties = TrieMap[Id[Party], List[RelatedParty]]()
  private val staticSecurityContexts = TrieMap[Id[User], SecurityContext]()
  
  def relatedParties(partyId : Id[Party]) : List[RelatedParty] = 
    relatedParties.getOrElseUpdate(partyId, relatedParties(partyId, Set.empty))
    
  def staticSecurityContext(userId : Id[User]) =
    staticSecurityContexts.getOrElseUpdate(userId, userAuthenticator.securityContextFor(userId).await(1.minute))
    
  private def relatedParties(partyId : Id[Party], exclude : Set[Id[Party]]) : List[RelatedParty] = {
    if (!exclude.contains(partyId)) 
      retrieve(partyId, 1.minute).map(_.entity) map { root =>
        val v2 = exclude ++ Set(partyId)
        root.relations ++ root.relations.flatMap(p => relatedParties(p.relatedParty, v2))
      } getOrElse(Nil)
    else Nil
  }
  
  override def invalidate(id: Id[Party]) : Unit = {
    super.invalidate(id)
    relatedParties.clear
    staticSecurityContexts.clear
  }
}