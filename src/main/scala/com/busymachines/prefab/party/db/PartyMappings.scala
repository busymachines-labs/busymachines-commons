package com.busymachines.prefab.party.db

import com.busymachines.commons.elasticsearch.{ESField, ESMapping}
import com.busymachines.commons.implicits._
import com.busymachines.prefab.party.domain._
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.domain.Person
import com.busymachines.prefab.party.domain.PhoneNumber
import com.busymachines.prefab.party.domain.Company
import com.busymachines.prefab.party.domain.UserRole
import com.busymachines.prefab.party.domain.EmailAddress
import com.busymachines.prefab.party.domain.RelatedParty
import com.busymachines.prefab.party.implicits._
import com.busymachines.commons.domain.Id
import com.busymachines.prefab.authentication.model.Credentials

object PartyMapping extends ESMapping[Party] {
  val id = "_id" -> "id" :: String.as[Id[Party]] & NotAnalyzed
  val tenant = "tenant" :: String & NotAnalyzed
  val owner = "owner" :: String & NotAnalyzed
  
  val fdcWmsClientNumber = "fdcWmsClientNumber" :: String & NotAnalyzed
  val fdcIdfNumber = "fdcIdfNumber" :: String & NotAnalyzed
  val fdcDeliveryAddressCustomerNumber = "fdcDeliveryAddressCustomerNumber" :: String & NotAnalyzed

  val person = "person" :: Nested(PersonMapping)
  val company = "company" :: Nested(CompanyMapping)
  val addresses = "addresses" :: Nested(AddressMapping)
  val locations = "locations" :: Nested(PartyLocationMapping)
  val phoneNumbers = "phoneNumbers" :: Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" :: Nested(EmailMapping)
  val relations = "relations" :: Nested(RelatedPartyMapping)
  val users = "users" :: Nested(UserMapping)
  val userRoles = "userRoles" :: Nested(UserRoleMapping)
  val extensions = "extensions" :: String // no extension support yet in mapping framework
}

object PersonMapping extends ESMapping[Person] {
  val title = "title" :: String & NotAnalyzed
  val initials = "initials" :: String & NotAnalyzed
  val firstName = "firstName" :: String & NotAnalyzed
  val middleName = "middleName" :: String & NotAnalyzed
  val lastName = "lastName" :: String & NotAnalyzed
}

object CompanyMapping extends ESMapping[Company] {
  val name = "name" :: String & NotAnalyzed
}

object AddressMapping extends ESMapping[Address] {
  val street = "street" :: String & Analyzed
  val street2 = "street2" :: String & Analyzed
  val postalCode = "postalCode" :: String & NotAnalyzed
  val houseNumber = "houseNumber" :: String & NotAnalyzed
  val city = "city" :: String & Analyzed
  val country = "country" :: String & NotAnalyzed
  val kind = "kind" :: String & NotAnalyzed
  val comment = "comment" :: String & Analyzed
  val geoLocation = "geoLocation" :: GeoPoint
}

object PartyLocationMapping extends ESMapping[PartyLocation] {
  val id = "id" :: String & NotAnalyzed
  val description= "description" :: String & NotAnalyzed
  val address= "address" :: Nested(AddressMapping)
  val contactPerson= "contactPerson" :: String & NotAnalyzed
  val mainLocation="mainLocation" :: Boolean & NotAnalyzed
}

object PhoneNumberMapping extends ESMapping[PhoneNumber] {
  val email = "phoneNumber" :: String & Analyzed
  val kind = "kind" :: String & NotAnalyzed
}

object EmailMapping extends ESMapping[EmailAddress] {
  val kind = "kind" :: String & NotAnalyzed
  val validated = "validated" :: Boolean & NotAnalyzed
  val emailAddress = "emailAddress" :: String & Analyzed
}

object RelatedPartyMapping extends ESMapping[RelatedParty] {
  val relatedParty = "relatedParty" :: String & NotAnalyzed
  val relatedPartyAlias = "relatedPartyAlias" :: String & NotAnalyzed
  val kind = "kind" :: String & NotAnalyzed
  val role = "role" :: String & NotAnalyzed
}

object UserMapping extends ESMapping[User] {
  val id : ESField[User, Id[User]] = "_id" -> "id" :: String.as[Id[User]] & NotAnalyzed
  val credentials = "credentials" :: String.as[Id[Credentials]] & NotAnalyzed
  val firstName = "firstName" :: String & Analyzed
  val middleName = "middleName" :: String & Analyzed
  val lastName = "lastName" :: String & Analyzed
  val addresses = "addresses" :: Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" :: Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" :: Nested(EmailMapping)
  val roles = "roles" :: String & NotAnalyzed
}

object UserRoleMapping extends ESMapping[UserRole] {
  val id = "_id" -> "id" :: String.as[Id[UserRole]] & NotAnalyzed
  val name = "name" :: String & Analyzed
  val permissions = "permissions" :: String & NotAnalyzed
}
