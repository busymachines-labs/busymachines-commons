package com.busymachines.prefab.party.domain

import com.busymachines.commons
import com.busymachines.commons.implicits._
import scala.util.Random
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import com.busymachines.prefab.authentication.model.Credentials
import com.busymachines.commons.Extensions

case class Tenant(
  id : Id[Tenant], 
  name : String) extends HasId[Tenant]

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

  relations : List[RelatedParty] = Nil,

  /**
   * Users of the party
   */
  users : List[User] = Nil,
  
  /**
   * The user roles that available for the users in this party.
   */ 
  userRoles : List[UserRole] = Nil,

  extensions: Extensions[Party] = Extensions.empty
) extends HasId[Party] {

  def describe = 
    person.map(p => p.firstName :: p.middleName :: p.lastName :: Nil mkString " ")
    .orElse(company.map(_.name)).getOrElse("")
}
  
case class Company(
  name : String
)

case class Person(
  title : Option[String] = None,
  initials : Option[String] = None,
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
  /**
   * The user roles (from the party) to which this user belongs.
   */ 
  roles : List[Id[UserRole]] = Nil
) extends HasId[User] {

  def describe = 
    emailAddresses.find(_.kind.isEmpty).orElse(emailAddresses.headOption).map(_.emailAddress).
      getOrElse((firstName ++ middleName ++ lastName).mkString(" "))

}
  
case class Address (
  kind : Option[AddressKind] = None,
  street: Option[String] = None,
  houseNumber: Option[String] = None,
  street2: Option[String] = None, 
  postalCode: Option[String] = None, 
  city: Option[String] = None, 
  country : Option[String] = None,
  comment : Option[String] = None)

case class PhoneNumber (
  kind : Option[PhoneNumberKind] = None,
  phoneNumber : String)

case class EmailAddress (
  kind : Option[EmailAddressKind] = None,
  validated : Boolean,
  emailAddress : String
)

case class RelationKind(name : String) {
  override def toString = name
}
case class AddressKind(name : String) {
  override def toString = name
}
case class PhoneNumberKind(name : String) {
  override def toString = name
}
case class EmailAddressKind(name : String) {
  override def toString = name
}
case class Permission(name : String) {
  override def toString = name
}


