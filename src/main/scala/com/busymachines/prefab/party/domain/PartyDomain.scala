package com.busymachines.prefab.party.domain

import com.busymachines.commons
import com.busymachines.commons.implicits._
import scala.util.Random
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import com.busymachines.prefab.authentication.model.Credentials

case class Tenant(id : Id[Tenant], name : String) extends HasId[Tenant]

case class Party (
  
  id : Id[Party],
  
  tenant : Id[Tenant],
  
  /**
   * Owner party manages this party.
   */
  owner : Option[Id[Party]] = None, 
  
  person : Option[Person] = None,
  
  company : Option[Company] = None,
  
  /**
   * When there are multiple addresses of the same type, 
   * always use the first one.
   */
  addresses : List[Address] = Nil,
  
  phoneNumbers : List[PhoneNumber] = Nil,
  
  emailAddresses : List[EmailAddress] = Nil,

  /**
   * Party is allowed to create assets and child-models of these models.
   * Party can see all child models and their properties.
   * Party can see all properties of parent models.
   * Party can only change models it owns (AssetModel.owner)
   */
//  visibleAssetModels : List[Id[CatalogItem]] = Nil,
  
  relations : List[RelatedParty] = Nil,
  
  /**
   * Users of the party
   */
  users : List[User] = Nil,
  
  userRoles : List[UserRole] = Nil
) extends HasId[Party]

case class Company(
  name : String
)

case class Person(
  firstName : Option[String] = None,
  middleName : Option[String] = None,
  lastName : Option[String] = None
)

case class RelatedParty (
  kind : RelationKind,
  role : Id[PartyRole],
  relatedParty : Id[Party],
  relatedPartyAlias : String
)

case class PartyRole (
  id : Id[UserRole] = Id.generate,
  name : String,
  permissions : List[Permission]
)

case class RelationKind(kind : String)


case class UserRole (
  id : Id[UserRole] = Id.generate,
  name : String,
  permissions : List[Permission]
)

case class User (
  id : Id[User] = Id.generate,
  credentials : Id[Credentials] = Id.generate,
  firstName : Option[String] = None,
  middleName : Option[String] = None,
  lastName : Option[String] = None,
  addresses : List[Address] = Nil,
  phoneNumbers : List[PhoneNumber] = Nil,
  emailAddresses : List[EmailAddress] = Nil,
  roles : List[Id[UserRole]] = Nil
) extends HasId[User] {

  def describe = 
    emailAddresses.find(_.kind.isEmpty).orElse(emailAddresses.headOption).map(_.emailAddress).
      getOrElse((firstName ++ middleName ++ lastName).mkString(" "))

}
  
case class Address (
  kind : Option[AddressKind] = None,
  street: Option[String] = None, 
  street2: Option[String] = None, 
  postalCode: Option[String] = None, 
  city: Option[String] = None, 
  country : Option[String] = None)

case class PhoneNumber (
  kind : Option[PhoneNumberKind] = None,
  phoneNumber : String)

case class EmailAddress (
  kind : Option[EmailAddressKind] = None,
  validated : Boolean,
  emailAddress : String
)

case class AddressKind(_kind : String)
case class PhoneNumberKind(_kind : String)
case class EmailAddressKind(_kind : String)
case class Permission(_permission : String)


