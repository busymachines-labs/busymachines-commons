package com.kentivo.mdm.logic

import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.model.Authentication
import com.busymachines.prefab.authentication.logic.PrefabSecurityContext
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.Party

case class SecurityContext(
  partyId: Id[Party],
  userId: Id[User],
  authenticationId: Id[Authentication],
  permissions: Set[Permission.Value] = Set.empty) extends PrefabSecurityContext[Permission.Value] {

  def principalDescription: String = partyId.toString + "-" + userId.toString
}

object Permission extends Enumeration {

  /* Firmware permissions */
  val canCreateAnyFirmware = Value("canCreateAnyFirmware")
  val canDeleteAnyFirmware = Value("canDeleteAnyFirmware")
  val canUpdateAnyFirmware = Value("canUpdateAnyFirmware")
  val canViewAnyFirmware = Value("canViewAnyFirmware")

  /* Company permissions */
  val canCreateAnyCompany = Value("canCreateAnyCompany")
  val canViewAnyCompany = Value("canViewAnyCompany")
  val canDeleteAnyCompany = Value("canDeleteAnyCompany")
  val canUpdateAnyCompany = Value("canUpdateAnyCompany")

  /* Warehouse permissions */
  val canCreateAnyWarehouse = Value("canCreateAnyWarehouse")
  val canViewAnyWarehouse = Value("canViewAnyWarehouse")
  val canDeleteAnyWarehouse = Value("canDeleteAnyWarehouse")
  val canUpdateAnyWarehouse = Value("canUpdateAnyWarehouse")

  /* Employee permissions */
  val canCreateAnyEmployee = Value("canCreateAnyEmployee")
  val canDeleteAnyEmployee = Value("canDeleteAnyEmployee")
  val canUpdateAnyEmployee = Value("canUpdateAnyEmployee")
  val canUpdateSelfEmployee = Value("canUpdateSelfEmployee")
  val canViewAnyEmployee = Value("canViewAnyEmployee")
  val canViewSelfEmployee = Value("canViewSelfEmployee")
  val canListAnyEmployees = Value("canListAnyEmployee")

  /* Customer permissions */
  val canCreateAnyCustomer = Value("canCreateAnyCustomer")
  val canDeleteAnyCustomer = Value("canDeleteAnyCustomer")
  val canUpdateAnyCustomer = Value("canUpdateAnyCustomer")
  val canUpdateSelfCustomer = Value("canUpdateSelfCustomer")
  val canViewAnyCustomer = Value("canViewAnyCustomer")
  val canViewSelfCustomer = Value("canViewSelfCustomer")
  val canListAnyCustomers = Value("canListAnyCustomers")

  /* Device permissions */
  val canCreateAnyDevice = Value("canCreateAnyDevice")
  val canDeleteAnyDevice = Value("canDeleteAnyDevice")
  val canUpdateAnyDevice = Value("canUpdateAnyDevice")
  val canViewAnyDevice = Value("canViewAnyDevice")
  val canViewSelfDevice = Value("canViewSelfDevice")

  /* Customer saving target */
  val canCreateAnySavingTarget = Value("canCreateAnySavingTarget")
  val canDeleteAnySavingTarget = Value("canDeleteAnySavingTarget")
  val canUpdateAnySavingTarget = Value("canUpdateAnySavingTarget")
  val canUpdateSelfSavingTarget = Value("canUpdateSelfSavingTarget")
  val canViewAnySavingTarget = Value("canViewAnySavingTarget")
  val canViewSelfSavingTarget = Value("canViewSelfSavingTarget")

  /* Group permissions */
  val canCreateAnyGroup = Value("canCreateAnyGroup")
  val canDeleteAnyGroup = Value("canDeleteAnyGroup")
  val canUpdateAnyGroup = Value("canUpdateAnyGroup")
  val canViewAnyGroup = Value("canViewAnyGroup")
  val canListAnyGroup = Value("canListAnyGroup")
  val canViewSelfGroupMembership = Value("canViewSelfGroupMembership")
  val canViewSelfGroupAggregates = Value("canViewSelfGroupAggregates")

  /* Media permissions */
  val canCreateAnyMedia = Value("canCreateAnyMedia")
  val canUpdateAnyMedia = Value("canUpdateAnyMedia")
  val canDeleteAnyMedia = Value("canDeleteAnyMedia")
  val canViewAnyMedia = Value("canViewAnyMedia")

  /* Measurements permissions */
  val canCreateAnyMeasurements = Value("canCreateAnyMeasurements")
  val canCreateSelfMeasurements = Value("canCreateSelfMeasurements")
  val canDeleteAnyMeasurements = Value("canDeleteAnyMeasurements")
  val canDeleteSelfMeasurements = Value("canDeleteSelfMeasurements")
  val canUpdateAnyMeasurements = Value("canUpdateAnyMeasurements")
  val canUpdateSelfMeasurements = Value("canUpdateSelfMeasurements")
  val canViewAnyMeasurements = Value("canViewAnyMeasurements")
  val canViewSelfMeasurements = Value("canViewSelfMeasurements")
  val canViewAnyMeasurementsAggregates = Value("canViewAnyMeasurementsAggregates")
  val canViewSelfMeasurementsAggregates = Value("canViewSelfMeasurementsAggregates")

}

case class AuthenticationToken(token: String)

case class SecurityRole(id: Id[SecurityRole], name: String, permissions: Set[Permission.Value])

object AdminSecurityRole extends SecurityRole(Id[SecurityRole]("aabbe22e-bb6c-4602-aef9-b9f5dad15657"), "Admin", Permission.values)
object EmployeeSecurityRole extends SecurityRole(Id[SecurityRole]("aabbe22e-bb6c-4602-aef9-b9f5dad15658"), "Employee", Permission.values)
object UserSecurityRole extends SecurityRole(Id[SecurityRole]("aabbe22e-bb6c-4602-aef9-b9f5dad15659"), "User",
  Set(Permission.canViewSelfCustomer,
    Permission.canViewSelfDevice,
    Permission.canUpdateSelfSavingTarget,
    Permission.canViewSelfSavingTarget,
    Permission.canViewSelfGroupMembership,
    Permission.canViewSelfGroupAggregates,
    Permission.canViewSelfMeasurements,
    Permission.canViewSelfMeasurementsAggregates))

case class EncryptionKey(bytes: Array[Byte]) {
  override def toString() = {
    bytes.map("%02X" format _).mkString.grouped(8).toList.mkString("-")
  }
}
