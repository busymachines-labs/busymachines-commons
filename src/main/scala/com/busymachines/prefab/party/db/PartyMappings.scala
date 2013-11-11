package com.busymachines.prefab.party.db

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.party.domain.RelatedParty
import com.busymachines.prefab.party.domain.PhoneNumber
import com.busymachines.prefab.party.domain.UserRole
import com.busymachines.prefab.party.domain.EmailAddress
import com.busymachines.prefab.party.domain.Party
import com.busymachines.prefab.party.domain.Address
import com.busymachines.prefab.party.domain.User

object PartyMapping extends ESMapping[Party] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val owner = "owner" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val addresses = "addresses" as Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" as Nested(PhoneNumberMapping)
  val emailAddresses = "emailAddresses" as Nested(EmailMapping)
  val relations = "relations" as Nested(RelatedPartyMapping)
  val users = "users" as Nested(UserMapping)
  val userRoles = "userRoles" as Nested(UserRoleMapping)
}

object AddressMapping extends ESMapping[Address] {
  val street = "street" as String & Analyzed
  val street2 = "street2" as String & Analyzed
  val postalCode = "postalCode" as String & NotAnalyzed
  val city = "city" as String & Analyzed
  val country = "country" as String & NotAnalyzed
  val kind = "kind" as String & NotAnalyzed
}

object PhoneNumberMapping extends ESMapping[PhoneNumber] {
  val email = "phoneNumber" as String & Analyzed
  val kind = "kind" as String & NotAnalyzed
}

object EmailMapping extends ESMapping[EmailAddress] {
  val email = "email" as String & Analyzed
  val kind = "kind" as String & NotAnalyzed
}

object RelatedPartyMapping extends ESMapping[RelatedParty] {
  val relatedParty = "relatedParty" as String & NotAnalyzed
  val relatedPartyAlias = "relatedPartyAlias" as String & NotAnalyzed
  val relationType = "relationType" as String & NotAnalyzed
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
  val logins = "logins" as String & NotAnalyzed
}

object UserRoleMapping extends ESMapping[UserRole] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val permissions = "permissions" as String & NotAnalyzed
}
