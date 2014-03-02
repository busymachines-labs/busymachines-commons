package com.busymachines.prefab.party.db

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.party.domain.RelatedParty
import com.busymachines.prefab.party.domain.PhoneNumber
import com.busymachines.prefab.party.domain.UserRole
import com.busymachines.prefab.party.domain.EmailAddress
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.Address
import com.busymachines.prefab.party.domain.User
import com.busymachines.prefab.party.domain.Person
import com.busymachines.prefab.party.domain.Company

object PartyMapping extends ESMapping[Party] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val tenant = "tenant" as String & NotAnalyzed
  val owner = "owner" as String & NotAnalyzed
  val person = "person" as Nested(PersonMapping)
  val company = "company" as Nested(CompanyMapping)
  val addresses = "addresses" as Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" as Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" as Nested(EmailMapping)
  val relations = "relations" as Nested(RelatedPartyMapping)
  val wmsClientNumber = "wmsClientNumber" as String & NotAnalyzed
  val idfNumber = "idfNumber" as String & NotAnalyzed
  val users = "users" as Nested(UserMapping)
  val userRoles = "userRoles" as Nested(UserRoleMapping)
}

object PersonMapping extends ESMapping[Person] {
  val title = "title" as String & NotAnalyzed
  val initials = "initials" as String & NotAnalyzed
  val firstName = "firstName" as String & NotAnalyzed
  val middleName = "middleName" as String & NotAnalyzed
  val lastName = "lastName" as String & NotAnalyzed
}

object CompanyMapping extends ESMapping[Company] {
  val name = "name" as String & NotAnalyzed
}

object AddressMapping extends ESMapping[Address] {
  val street = "street" as String & Analyzed
  val street2 = "street2" as String & Analyzed
  val postalCode = "postalCode" as String & NotAnalyzed
  val houseNumber = "houseNumber" as String & NotAnalyzed
  val city = "city" as String & Analyzed
  val country = "country" as String & NotAnalyzed
  val kind = "kind" as String & NotAnalyzed
  val comment = "comment" as String & Analyzed
}

object PhoneNumberMapping extends ESMapping[PhoneNumber] {
  val email = "phoneNumber" as String & Analyzed
  val kind = "kind" as String & NotAnalyzed
}

object EmailMapping extends ESMapping[EmailAddress] {
  val kind = "kind" as String & NotAnalyzed
  val validated = "validated" as Boolean & NotAnalyzed
  val emailAddress = "emailAddress" as String & Analyzed
}

object RelatedPartyMapping extends ESMapping[RelatedParty] {
  val relatedParty = "relatedParty" as String & NotAnalyzed
  val relatedPartyAlias = "relatedPartyAlias" as String & NotAnalyzed
  val kind = "kind" as String & NotAnalyzed
  val role = "role" as String & NotAnalyzed
}

object UserMapping extends ESMapping[User] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val credentials = "credentials" as String & NotAnalyzed 
  val firstName = "firstName" as String & Analyzed
  val middleName = "middleName" as String & Analyzed
  val lastName = "lastName" as String & Analyzed
  val addresses = "addresses" as Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" as Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" as Nested(EmailMapping)
  val roles = "roles" as String & NotAnalyzed
}

object UserRoleMapping extends ESMapping[UserRole] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val permissions = "permissions" as String & NotAnalyzed
}
