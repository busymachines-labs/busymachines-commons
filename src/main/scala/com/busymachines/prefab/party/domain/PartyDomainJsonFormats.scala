package com.busymachines.prefab.party.domain

import com.busymachines.commons.domain.CommonJsonFormats

object PartyDomainJsonFormats extends PartyDomainJsonFormats

trait PartyDomainJsonFormats extends CommonJsonFormats {

  implicit val phoneNumberKindFormat = stringFormat[PhoneNumberKind]("PhoneNumberKind", PhoneNumberKind, _._kind)
  implicit val addressKindFormat = stringFormat[AddressKind]("AddressKind", AddressKind(_), _._kind)
  implicit val emailKindFormat = stringFormat[EmailAddressKind]("EmailKind", EmailAddressKind(_), _._kind)
  implicit val relationFormat = stringFormat[RelationKind]("RelationKind", RelationKind(_), _.kind)
  implicit val permissionFormat = stringFormat[Permission]("Permission", Permission(_), _._permission)
  implicit val addressFormat = jsonFormat6(Address)
  implicit val phoneNumberFormat = jsonFormat2(PhoneNumber)
  implicit val emailFormat = jsonFormat3(EmailAddress)
  implicit val userFormat = jsonFormat9(User)
  implicit val userRoleFormat = jsonFormat3(UserRole)
  implicit val partyRoleFormat = jsonFormat3(PartyRole)
  implicit val relatedPartyFormat = jsonFormat4(RelatedParty)
  implicit val tenantFormat = jsonFormat2(Tenant)
  implicit val companyFormat = jsonFormat1(Company)
  implicit val personFormat = jsonFormat3(Person)
  implicit val partyFormat = jsonFormat11(Party)
}