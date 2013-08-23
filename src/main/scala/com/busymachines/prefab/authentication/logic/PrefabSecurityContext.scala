package com.busymachines.prefab.authentication.logic

import com.busymachines.commons.NotAuthorizedException

/**
 * A security context may inherit from CommonSecurityContext. Since it throws
 * NotAuthorizedException, the CommonExceptionHandler will translate this to the right
 * HTTP error code. Beyond this, there is no need to inherit this class.
 * A security context should be an implicit parameter to all logic-layer components.
 */
trait PrefabSecurityContext[Permission] {

  def principalDescription : String
  def permissions : Set[Permission]

  def isAllowedTo(permission : Permission) : Boolean =
    permissions.contains(permission)

    /**
     * Permissions should be OR-ed
     * TODO Paul make *, and 
     */
  def mustBeAllowedTo(permission : Permission) : Unit =
    if (!permissions.contains(permission)) {
      throw new NotAuthorizedException(s"printPrincipal has no permission to $permission")
    }
}

