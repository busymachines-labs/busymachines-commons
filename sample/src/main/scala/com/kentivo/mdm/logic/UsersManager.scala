package com.kentivo.mdm.logic

import com.kentivo.mdm.commons.InvalidIdException
import com.kentivo.mdm.domain.User
import com.busymachines.commons.domain.Id

object UsersManager {

  /**
   * Find a specific user by id.
   */
  def find(entityId: Id[User]): User = {
   null
  }

  /**
   * Update a specific user.
   */
  def update(entityId: Id[User], user: User): String = {
    ""
  }
}
