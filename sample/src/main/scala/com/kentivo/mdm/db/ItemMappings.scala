package com.kentivo.mdm.db

import org.elasticsearch.client.Client
import com.busymachines.commons.elasticsearch.ESMapping
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyValue
import com.kentivo.mdm.domain.Source
import com.kentivo.mdm.domain.RelatedParty
import com.busymachines.commons.elasticsearch.ESIndex
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import java.util.Locale
import com.kentivo.mdm.domain.Party
import com.kentivo.mdm.domain.Address
import com.kentivo.mdm.domain.PhoneNumber
import com.kentivo.mdm.domain.Email
import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.UserRole

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

object EmailMapping extends ESMapping[Email] {
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
  val firstName = "firstName" as String & Analyzed
  val middleName = "middleName" as String & Analyzed
  val lastName = "lastName" as String & Analyzed
  val addresses = "addresses" as Nested(AddressMapping)
  val phoneNumbers = "phoneNumbers" as Nested(PhoneNumberMapping)
  val primaryEmail = "primaryEmail" as String & Analyzed 
  val emailAddresses = "emailAddresses" as Nested(EmailMapping)
  val roles = "roles" as String & NotAnalyzed
  val logins = "logins" as String & NotAnalyzed
}

object UserRoleMapping extends ESMapping[UserRole] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val permissions = "permissions" as String & NotAnalyzed
}

object ItemMapping extends ESMapping[Item] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val repository = "repository" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val owner = "owner" as String & NotAnalyzed
  val name = "name" as Object[Map[Locale, String]] & Analyzed
  val parent = "parents" as String & NotAnalyzed
  val properties = "properties" as Nested(PropertyMapping) 
  val values = "values" as Nested(PropertyValueMapping) 
}

object PropertyMapping extends ESMapping[Property] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val repository = "repository" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val name = "name" as Object[Map[Locale, String]] & Analyzed
  val scope = "scope" as String & NotAnalyzed
  val `type` = "type" as String & NotAnalyzed
  val defaultUnit = "defaultUnit" as String & NotAnalyzed
  val itemValueBase = "itemValueBase" as String & NotAnalyzed
  val mandatory = "mandatory" as Boolean & NotAnalyzed
  val multiValue = "multiValue" as Boolean & NotAnalyzed
  val multiLingual = "multiLingual" as Boolean & NotAnalyzed
  val groups = "groups" as String & NotAnalyzed
  val rules = "rules" as String & NotAnalyzed
}

object PropertyValueMapping extends ESMapping[PropertyValue] {
  val property = "property" as String & NotAnalyzed
  val mutation = "mutation" as String & NotAnalyzed
  val value = "value" as String & Analyzed
  val locale = "locale" as String & NotAnalyzed
  val unit = "unit" as String & NotAnalyzed
}

object SourceMapping extends ESMapping[Source] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val name = "name" as String & Analyzed
  val repository = "repository" as String & NotAnalyzed
  val model = "model" as Nested(ItemMapping)
}

