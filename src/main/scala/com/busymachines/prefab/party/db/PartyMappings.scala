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
  val id = "_id" -> "id" :: String.as[Id[Party]]
  val tenant = "tenant" :: String
  val owner = "owner" :: String
  
  val fdcWmsClientNumber = "fdcWmsClientNumber" :: String
  val fdcIdfNumber = "fdcIdfNumber" :: String
  val fdcDeliveryAddressCustomerNumber = "fdcDeliveryAddressCustomerNumber" :: String

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
  val title = "title" :: String
  val initials = "initials" :: String
  val firstName = "firstName" :: String
  val middleName = "middleName" :: String
  val lastName = "lastName" :: String
}

object CompanyMapping extends ESMapping[Company] {
  val name = "name" :: String
}

object AddressMapping extends ESMapping[Address] {
  val street = "street" :: String
  val street2 = "street2" :: String
  val postalCode = "postalCode" :: String
  val houseNumber = "houseNumber" :: String
  val city = "city" :: String
  val country = "country" :: String
  val kind = "kind" :: String
  val comment = "comment" :: String & Analyzed
  val geoLocation = "geoLocation" :: GeoPoint
}

object PartyLocationMapping extends ESMapping[PartyLocation] {
  val id = "id" :: String
  val description= "description" :: String
  val address= "address" :: Nested(AddressMapping)
  val contactPerson= "contactPerson" :: String
  val mainLocation="mainLocation" :: Boolean
}

object PhoneNumberMapping extends ESMapping[PhoneNumber] {
  val phoneNumber = "phoneNumber" :: String
  val kind = "kind" :: String
}

object EmailMapping extends ESMapping[EmailAddress] {
  val kind = "kind" :: String
  val validated = "validated" :: Boolean
  val emailAddress = "emailAddress" :: String
}

object RelatedPartyMapping extends ESMapping[RelatedParty] {
  val relatedParty = "relatedParty" :: String
  val relatedPartyAlias = "relatedPartyAlias" :: String
  val kind = "kind" :: String
  val role = "role" :: String
}

object UserMapping extends ESMapping[User] {
  val id : ESField[User, Id[User]] = "_id" -> "id" :: String.as[Id[User]]
  val credentials = "credentials" :: String.as[Id[Credentials]]
  val firstName = "firstName" :: String
  val middleName = "middleName" :: String
  val lastName = "lastName" :: String
  val addresses = "addresses" :: Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" :: Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" :: Nested(EmailMapping)
  val roles = "roles" :: String
}

object UserRoleMapping extends ESMapping[UserRole] {
  val id = "_id" -> "id" :: String.as[Id[UserRole]]
  val name = "name" :: String
  val permissions = "permissions" :: String
}
