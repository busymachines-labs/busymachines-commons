package com.busymachines.commons.domain


/**
 * TODO
 * - determine compatibility of Units of of measure. For example, all locale-specific units of a property must be compatible.
 * - need to auto-convert between compatible units
 * - add non-si units, like inches, pounds, miles
 */
case class UnitOfMeasure(terms : List[UnitOfMeasureTerm]) {
  def symbol = terms.mkString("\u22C5")
}

case class UnitOfMeasureTerm(prefix : UnitPrefix.Prefix, baseUnit : UnitOfMeasure.Unit, exponent : Integer) {
  def symbol = prefix.symbol + baseUnit.symbol + exponent
}

object UnitOfMeasure extends Enumeration {
  case class Unit(name : String, symbol : String) extends Val(name)
  val Metre = Unit("metre", "m")
  val Gram = Unit("gram", "g")
  val Second = Unit("second", "s")
  val Ampere = Unit("ampere", "a")
  val Kelvin = Unit("kelvin", "k")
  val Mole = Unit("mole", "m")
  val Candela = Unit("candela", "c")

  val Hertz = Unit("hertz", "Hz")
  val Radian = Unit("radian", "rad")
  val Steradian = Unit("steradian", "sr")
  val Newton = Unit("newton", "N")
  val Pascal = Unit("pascal", "Pa")
  val Joule = Unit("joule", "J")
  val Watt = Unit("watt", "W")
  val Coulomb = Unit("coulomb", "C")
  val Volt = Unit("volt", "V")
  val Farad = Unit("farad", "F")
  val Ohm = Unit("ohm", "\u2126")
  val Siemens = Unit("siemens", "S")
  val Weber = Unit("weber", "Wb")
  val Tesla = Unit("tesla", "T")
  val Henry = Unit("henry", "H")
  val Celcius = Unit("celcius", "\u00B0C")
  val Lumen = Unit("lumen", "lm")
  val Lux = Unit("lux", "lx")
  val Becquerel = Unit("becquerel", "Bq")
  val Gray = Unit("gray", "Gy")
  val Sievert = Unit("sievert", "Sv")
  val Katal = Unit("katal", "kat")
  
  val Minute = Unit("minute", "min")
  val Hour = Unit("hour", "h")
  val Day = Unit("day", "d")
  val AngleDegree = Unit("angle-degree", "\u00B0")
  val AngleMinute = Unit("angle-minute", "'")
  val AngleSecond = Unit("angle-second", "\"")
  val Hectare = Unit("hectare", "ha")
  val Litre = Unit("litre", "l")
  val Tonne = Unit("tomme", "t")
  val Bel = Unit("bel", "B")
  val ElectronVolt = Unit("electron-volt", "eV")
  
  val Angstrom = Unit("angstrom", "A")
  val Are = Unit("are", "a")
  val Barn = Unit("barn", "b")
  val Bar = Unit("bar", "bar")
  val Atmosphere = Unit("atmosphere", "atm")
  val Torr = Unit("torr", "Torr")
}

object UnitPrefix extends Enumeration {
  case class Prefix(name : String, symbol : String, factor : Double) extends Val(name)
  val Yotta = Prefix("yotta", "Y", 10^24)
  val Zetta = Prefix("zetta", "Z", 10^21)
  val Exa = Prefix("exa", "E", 10^18)
  val Peta = Prefix("peta", "P", 10^15)
  val Tera = Prefix("tera", "T", 10^12)
  val Giga = Prefix("giga", "G", 10^9)
  val Mega = Prefix("mega", "M", 10^6)
  val Kilo = Prefix("kilo", "k", 10^3)
  val Hecto = Prefix("hecto", "h", 10^2)
  val Deca = Prefix("deca", "da", 10^1)
  val None = Prefix("none", "", 10^0)
  val Deci = Prefix("deci", "d", 10^(-1))
  val Centi = Prefix("centi", "c", 10^(-2))
  val Milli = Prefix("milli", "m", 10^(-3))
  val Micro = Prefix("micro", "\u00B5", 10^(-6))
  val Nano = Prefix("nano", "n", 10^(-9))
  val Pico = Prefix("pico", "p", 10^(-12))
  val Femto = Prefix("femto", "f", 10^(-15))
  val Atto = Prefix("atto", "a", 10^(-18))
  val Zepto = Prefix("zepto", "z", 10^(-21))
  val Yocto = Prefix("yocto", "y", 10^(-24))
}