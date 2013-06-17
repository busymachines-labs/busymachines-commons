package com.kentivo.mdm.domain

import com.busymachines.commons
import com.busymachines.commons.implicits._
import scala.util.Random
import com.busymachines.commons.domain.Id

case class Party (
  id : Id[Party] = Id.generate,
  owner : Option[Id[Party]] = None, 
  
  /**
   * name of the party
   */
  name : String,
  
  addresses : List[Address] = Nil,
  phoneNumbers : List[PhoneNumber] = Nil,
  emailAddresses : List[Email] = Nil,

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
)

case class RelatedParty (
  relatedParty : Id[Party],
  relatedPartyAlias : String,
  relationType : String 
//  allowedAssetModels : List[Id[CatalogItem]]
)

case class UserRole (
  id : Id[UserRole] = Id.generate,
  name : String,
  permissions : List[String]
)

case class User (
  id : Id[User] = Id.generate,
  logins : List[Id[Login]] = Nil,
  firstName : String = "",
  middleName : String = "",
  lastName : String = "",
  addresses : List[Address] = Nil,
  phoneNumbers : List[PhoneNumber] = Nil,
  emailAddresses : List[Email] = Nil,
  roles : List[Id[UserRole]] = Nil
)
  
case class Address (
  kind : Option[AddressKind.Value] = None,
  street: Option[String] = None, 
  street2: Option[String] = None, 
  postalCode: Option[String] = None, 
  city: Option[String] = None, 
  country : Option[String] = None)

object AddressKind extends Enumeration {
  val Home = Value("home")
  val Work = Value("work")
  val Invoice = Value("invoice")
  val Delivery = Value("delivery")
  val Other = Value("other")
}

case class PhoneNumber (
  kind : Option[PhoneNumberKind.Value] = None,
  phoneNumber : String)

object PhoneNumberKind extends Enumeration {
  val Home = Value("home")
  val Work = Value("work")
  val Other = Value("other")
  def other(tag : String) = Value(tag)
}

case class Email (
  kind : Option[EmailKind.Value] = None,
  email : String
)

object EmailKind extends Enumeration {
  val Home = Value("home")
  val Work = Value("work")
  val Other = Value("other")
}

case class Login (
  id : Id[Login] = Id.generate,
  email : String, 
  salt : String = Random.nextString(8),
  password : Array[Byte] = Array.empty) {
  
  def withPassword(password: String) = 
    this.copy(password = (salt + password).sha256Hash)
}

