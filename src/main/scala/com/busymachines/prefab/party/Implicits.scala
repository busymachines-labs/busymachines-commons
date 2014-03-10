package com.busymachines.prefab.party

import com.busymachines.prefab.party.domain._
import com.busymachines.prefab.party.db.{EmailMapping, UserMapping, PartyMapping}
import com.busymachines.commons.implicits._
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.PartyRole
import com.busymachines.prefab.party.domain.AddressKind
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.domain.RelationKind
import com.busymachines.prefab.party.domain.PhoneNumber
import com.busymachines.prefab.party.domain.Company
import com.busymachines.prefab.party.domain.UserRole
import com.busymachines.prefab.party.domain.Permission
import com.busymachines.prefab.party.domain.EmailAddress
import com.busymachines.prefab.party.domain.EmailAddressKind
import com.busymachines.prefab.party.domain.RelatedParty
import com.busymachines.prefab.party.domain.Tenant
import com.busymachines.prefab.party.domain.Address
import com.busymachines.prefab.party.domain.PhoneNumberKind
import com.busymachines.prefab.party.api.v1.model.{AuthenticationResponse, AuthenticationRequest}
import spray.json.{JsNull, JsValue, RootJsonFormat}
import com.busymachines.commons.Extensions

object implicits {

  implicit val phoneNumberKindFormat = stringFormat[PhoneNumberKind]("PhoneNumberKind", PhoneNumberKind, _.name)
  implicit val addressKindFormat = stringFormat[AddressKind]("AddressKind", AddressKind, _.name)
  implicit val emailKindFormat = stringFormat[EmailAddressKind]("EmailKind", EmailAddressKind, _.name)
  implicit val relationFormat = stringFormat[RelationKind]("RelationKind", RelationKind, _.name)
  implicit val permissionFormat = stringFormat[Permission]("Permission", Permission, _.name)
  implicit val addressFormat = jsonFormat9(Address)
  implicit val phoneNumberFormat = jsonFormat2(PhoneNumber)
  implicit val emailFormat = jsonFormat3(EmailAddress)
  implicit val userFormat = jsonFormat9(User)
  implicit val userRoleFormat = jsonFormat3(UserRole)
  implicit val partyRoleFormat = jsonFormat3(PartyRole)
  implicit val relatedPartyFormat = jsonFormat4(RelatedParty)
  implicit val tenantFormat = jsonFormat2(Tenant)
  implicit val companyFormat = jsonFormat1(Company)
  implicit val personFormat = jsonFormat5(Person)
  implicit val partyLocationFormat = jsonFormat5(PartyLocation)
  implicit val partyFormat = format16(Party)

  implicit val authenticationRequestFormat = jsonFormat2(AuthenticationRequest)
  implicit val authenticationResponseFormat = jsonFormat3(AuthenticationResponse)

  implicit def toPartyMapping(t: Party.type) = PartyMapping
  implicit def toUserMapping(t: User.type) = UserMapping
  implicit def toEmailMapping(t: EmailAddress.type) = EmailMapping
}