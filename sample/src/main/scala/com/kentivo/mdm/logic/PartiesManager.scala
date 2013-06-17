package com.kentivo.mdm.logic

import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.Party

object PartiesManager {

  def list(implicit user: User): List[Party] = {
    Nil
  }

  /**
   * Create a party based on specific fields received.
   */
  def create(party: Party)(implicit user: User): Int = {
    0
  }

  /**
   * Find a specific party by id.
   */
  def find(entityId: Int): Party = {
   null
  }

  /**
   * Delete a specific party based on its id.
   */
  def delete(entityId: Int): String = {
    ""
  }

  /**
   * To check if user has enough rights to use a specific party id for specific operations (eg. to create a location for this partyId) we have to
   * check if that party is the party of current user OR if it's a child party.
   */
  def userHasEnoughRights(partyId: Int, user: User) = {
    false
  }
}