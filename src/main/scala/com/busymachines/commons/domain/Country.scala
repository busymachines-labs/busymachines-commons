package com.busymachines.commons.domain

import com.busymachines.commons.{EnumValue, Enum}

/**
 *  For adding additional countries see list from:
 *  http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
 *
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11.07.2014.
 */
trait Country extends EnumValue[Country]
object Country extends Enum[Country] {
  case class Value(name: String, description: String) extends Val with NextId with Country
  val Austria = Value("AT", "Austria")
  val Belgium = Value("BE", "Belgium")
  val BosniaAndHerzegovina = Value("BA", "Bosnia And Herzegovina")
  val CzechRepublic = Value("CZ", "CzechRepublic")
  val Denmark = Value("DK", "Denmark")
  val Estonia = Value("EE", "Estonia")
  val Finland = Value("FI", "Finland")
  val France = Value("FR", "France")
  val Germany = Value("DE", "Germany")
  val Hungary = Value("HU", "Hungary")
  val Ireland = Value("IE", "Ireland")
  val Italy = Value("IT", "Italy")
  val Netherlands = Value("NL", "Netherlands")
  val Norway = Value("NO", "Norway")
  val Poland = Value("PL", "Poland")
  val Romania = Value("RO", "Romania")
  val RussianFederation = Value("RU", "RussianFederation")
  val Serbia = Value("CS", "Serbia")
  val Slovenia = Value("SI", "Slovenia")
  val Spain = Value("ES", "Spain")
  val Sweden = Value("SE", "Sweden")
  val Switzerland = Value("CH", "Switzerland")
  val UnitedKingdom = Value("UK", "United Kingdom")
  
  val UnitedStates = Value("US", "UnitedStates")
}
