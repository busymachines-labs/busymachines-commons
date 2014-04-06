package com.busymachines.commons.domain

import com.busymachines.commons.implicits._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object UnitOfMeasure extends UnitOfMeasureImpl {

  /**
   * Creates a new unit of measure by parsing its symbolic description.
   */
  def apply(s: String): UnitOfMeasure = 
    UnitOfMeasureParser.parse(s)
  
  def apply(terms: Term*): UnitOfMeasure = 
    UnitOfMeasure(terms.toList)

  def areCompatible(from: UnitOfMeasure, to: UnitOfMeasure): Boolean =
    from == to || from.normalized.withoutPrefix == to.normalized.withoutPrefix

  def areConvertible(from: UnitOfMeasure, to: UnitOfMeasure): Boolean =
    from == to || from.baseUnitNormalized.withoutPrefix == to.baseUnitNormalized.withoutPrefix
    
  def converter(from: UnitOfMeasure, to: UnitOfMeasure): Double => Double = 
    if (areConvertible(from, to)) {
      val factor = converterFactor(from, to)
      (value: Double) => value * factor
    }
    else throw new Exception(s"Cannot convert unit $from to $to")

  case class Term(prefix: Prefix, unit: Unit, exponent: Int = 1) {
    def ^(exponent: Int) = copy(exponent = exponent)
    def ^-(exponent: Int) = copy(exponent = -exponent)
    def ::(prefix: Prefix) = copy(prefix = prefix)
  }
  object Term {
    implicit def toUnitOfMeasure(term: Term) = UnitOfMeasure(List(term))
  }
  case class Prefix(name: String, symbol: String, exponent: Int) {
    val factor = math.pow(10, exponent)
  } 
  case class Unit(name: String, symbol: String) 
  object Unit {
    implicit def toTerm(unit: Unit) = Term(NoPrefix, unit, 1)
    implicit def toUnitOfMeasure(unit: Unit) = UnitOfMeasure(Term(NoPrefix, unit, 1))
  }
  
  val Yotta = prefix("yotta", "Y", 24)
  val Zetta = prefix("zetta", "Z", 21)
  val Exa = prefix("exa", "E", 18)
  val Peta = prefix("peta", "P", 15)
  val Tera = prefix("tera", "T", 12)
  val Giga = prefix("giga", "G", 9)
  val Mega = prefix("mega", "M", 6)
  val Kilo = prefix("kilo", "k", 3)
  val Hecto = prefix("hecto", "h", 2)
  val Deca = prefix("deca", "da", 1)
  val NoPrefix = prefix("", "", 0)
  val Deci = prefix("deci", "d", (-1))
  val Centi = prefix("centi", "c", (-2))
  val Milli = prefix("milli", "m", (-3))
  val Micro = prefix("micro", "\u00B5", (-6))
  val Nano = prefix("nano", "n", (-9))
  val Pico = prefix("pico", "p", (-12))
  val Femto = prefix("femto", "f", (-15))
  val Atto = prefix("atto", "a", (-18))
  val Zepto = prefix("zepto", "z", (-21))
  val Yocto = prefix("yocto", "y", (-24))

  val Metre = baseUnit("metre", "m")
  val Gram = baseUnit("gram", "g")
  val Second = baseUnit("second", "s")
  val Ampere = baseUnit("ampere", "A")
  val Kelvin = baseUnit("kelvin", "K")
  val Mole = baseUnit("mole", "mol")
  val Candela = baseUnit("candela", "cd")

  val Hertz = derivedUnit("hertz", "Hz", "s-1")
  val Radian = derivedUnit("radian", "rad", "m/m")
  val Steradian = derivedUnit("steradian", "sr", "m2/m2")
  val Newton = derivedUnit("newton", "N", "kg m/s2")
  val Pascal = derivedUnit("pascal", "Pa", "kg/m s2")
  val Joule = derivedUnit("joule", "J", "kg m2 s-2")
  val Watt = derivedUnit("watt", "W", "kg m2 s-3")
  val Coulomb = derivedUnit("coulomb", "C", "s A")
  val Volt = derivedUnit("volt", "V", "kg m2 s-3 A-1")
  val Farad = derivedUnit("farad", "F", "kg-1 m-2 s4 A2")
  val Ohm = derivedUnit("ohm", "\u2126", "kg m2 s-2 A-1")
  val Siemens = derivedUnit("siemens", "S", "kg-1 m-2 s3 A2")
  val Weber = derivedUnit("weber", "Wb", "kg m2 s-1 A-1")
  val Tesla = derivedUnit("tesla", "T", "kg s-2 A-1")
  val Henry = derivedUnit("henry", "H", "kg m2 s-2 A-2")
  val Celcius = derivedUnit("celcius", "\u00B0C", "K")
  val Lumen = derivedUnit("lumen", "lm", "cd")
  val Lux = derivedUnit("lux", "lx", "m-2 cd")
  val Becquerel = derivedUnit("becquerel", "Bq", "s-1")
  val Gray = derivedUnit("gray", "Gy", "m2 s-2")
  val Sievert = derivedUnit("sievert", "Sv", "m2 s-2")
  val Katal = derivedUnit("katal", "kat", "s-1 mol")
  
  val Minute = derivedUnit("minute", "min", "s", 60)
  val Hour = derivedUnit("hour", "h", "s", 3600)
  val Day = derivedUnit("day", "d", "s", 86400)
  val AngleDegree = derivedUnit("angle-degree", "\u00B0")
  val AngleMinute = derivedUnit("angle-minute", "'")
  val AngleSecond = derivedUnit("angle-second", "\"")
  val Hectare = derivedUnit("hectare", "ha")
  val Litre = derivedUnit("litre", "l")
  val Tonne = derivedUnit("tomme", "t")
  val Bel = derivedUnit("bel", "B")
  val ElectronVolt = derivedUnit("electron-volt", "eV")
  
  val Angstrom = derivedUnit("angstrom", "A")
  val Are = derivedUnit("are", "a")
  val Barn = derivedUnit("barn", "b")
  val Bar = derivedUnit("bar", "bar")
  val Atmosphere = derivedUnit("atmosphere", "atm")
  val Torr = derivedUnit("torr", "Torr")

  // Some commonly used units:
  val KiloWattHour = (Kilo::Watt) * Hour
  val SquareMeter = Metre^2
  val CubicMeter = Metre^3
}

case class UnitOfMeasure (terms: List[UnitOfMeasure.Term]) {
  import UnitOfMeasure._
  import UnitOfMeasureImpl._

  def *(term: Term) = copy(terms :+ term)
  
  /**
   * A normalized unit contains each unit exacly once, and where the terms have a fixed ordering.
   * A normalized unit can be used to determince compatibility.
   * Example: "m s m" has normalized form "m2 s"
   */
  lazy val normalized: UnitOfMeasure = 
    copy(terms.groupBy(_.unit.symbol).mapValues { ts =>
      val factor = ts.foldLeft(0)(_ + _.prefix.exponent)
      val prefix = prefixes.find(_.exponent == factor).getOrElse(throw new Exception(s"Couldn't find prefix for factor 10^$factor"))
      Term(prefix, ts.head.unit, ts.foldLeft(0)(_ + _.exponent))
    }.values.toList.sortBy(_.unit.symbol))
    
  /**
   * A normalized unit that additionally has all units converted to si base units.
   * Example: "kWh" has base-unit normalized form "Mg m2/s2"
   */
  lazy val baseUnitNormalized: UnitOfMeasure =
    copy(terms.flatMap(t => baseUnitMappings.get(t.unit.symbol).map { 
      case (ts, baseUnitFactor) =>
        val factor = t.prefix.exponent + ts.head.prefix.exponent
        val prefix = prefixes.find(_.exponent == factor).getOrElse(throw new Exception(s"Couldn't find prefix for factor 10^$factor"))
        ts.head.copy(prefix = prefix) :: ts.tail
    }.getOrElse(List(t)))).normalized
  lazy val baseUnitConversionFactor: Double = 
    terms.flatMap(t => baseUnitMappings.get(t.unit.symbol).map(_._2)).foldLeft(1d)(_ * _)
  lazy val symbol = 
    UnitOfMeasurePrinter.printSymbol(this)
  lazy val withoutPrefix: UnitOfMeasure =
    copy(terms.map(_.copy(prefix = NoPrefix)))
  override def toString = 
    symbol
}

object UnitOfMeasureImpl {
  import UnitOfMeasure._
  val prefixes = mutable.Set[Prefix]()
  val baseUnits = mutable.Set[Unit]()
  val units = mutable.SortedSet[Unit]()(Ordering.by(u => ('Z'-u.symbol.size) + u.symbol))
  val baseUnitMappings = mutable.Map[String, (List[Term], Double)]()
}

class UnitOfMeasureImpl {
  import UnitOfMeasureImpl._
  import UnitOfMeasure._

  protected def converterFactor(from: UnitOfMeasure, to: UnitOfMeasure) = 
    from.baseUnitNormalized.terms.zip(to.baseUnitNormalized.terms)
      .map(t => t._1.prefix.factor / t._2.prefix.factor)
      .foldLeft(from.baseUnitConversionFactor / to.baseUnitConversionFactor)(_ * _)
        
  protected def prefix(name: String, symbol: String, factor: Int) = {
    val prefix = Prefix(name, symbol, factor)
    prefixes += prefix
    prefix
  }
    
  protected def baseUnit(name: String, symbol: String) = {
    val unit = Unit(name, symbol)
    baseUnits += unit    
    units += unit    
    unit
  }
    
  protected def derivedUnit(name: String, symbol: String, baseUnits: String = "", factor: Double = 1) = {
    baseUnitMappings.put(symbol, (UnitOfMeasureParser.parse(baseUnits).terms, factor))
    val unit = Unit(name, symbol)
    units += unit    
    unit
  }
}

object UnitOfMeasurePrinter {
  import UnitOfMeasureImpl._
  def printSymbol(unit: UnitOfMeasure, separator: String = " ", exponentSign: String = "^") = {
    val nrOfNeg = unit.terms.count(_.exponent < 0)
    val shouldSlash = nrOfNeg == 1 && unit.terms.head.exponent >= 0
    var prev: String = ""
    unit.terms.map { term =>
      val prefix = term.prefix.map(_.symbol).getOrElse("")
      val symbol = term.unit.symbol
      val (sep, exp) =
        if (term.exponent < 0 && shouldSlash) ("/", -term.exponent)
        else (if (prev != "" && (prefixes.exists(_.symbol == prev) || Character.isLowerCase(prev.last) == Character.isLowerCase(prefix.headOption.orElse(symbol.headOption).getOrElse(' ')))) separator else "", term.exponent)
      if (exp != 0) {
        prev = prefix + symbol + (if (exp != 1) exp else "")
        sep + prev
      } else ""
    }.mkString
  }  
}

object UnitOfMeasureParser {
  
  import UnitOfMeasure._
  import UnitOfMeasureImpl._
  
  def parse(s: String): UnitOfMeasure = {
    val terms = ArrayBuffer[Term]()
    var pos = 0
    while (pos < s.length) {
      while (pos < s.length && Character.isWhitespace(s.charAt(pos))) pos += 1
      var neg = 1 
      if (pos < s.length && s.charAt(pos) == '/') { pos += 1; neg = -1 }
      val (term, newPos) = parseTerm(s, pos, neg)
      pos = newPos
      terms += term
      while (pos < s.length && Character.isWhitespace(s.charAt(pos))) pos += 1 
    }
    UnitOfMeasure(terms.toList)
  }
  
  private def parseTerm(s: String, pos: Int, neg: Int): (Term, Int) = {
    prefixes.find(p => p.symbol.nonEmpty && s.startsWith(p.symbol, pos)) match {
      case Some(prefix) =>
        units.find(u => s.startsWith(u.symbol, pos + prefix.symbol.length)) match {
          case Some(unit) =>
            parseExponent(s, pos + prefix.symbol.length + unit.symbol.length, neg: Int, prefix, unit)
          case None =>
            parseUnit(s, pos, neg)
        }
      case None =>
        parseUnit(s, pos, neg)
    }
  }
  
  private def parseUnit(s: String, pos: Int, neg: Int): (Term, Int) = {
    units.find(u => s.startsWith(u.symbol, pos)) match {
      case Some(unit) =>
        parseExponent(s, pos + unit.symbol.length, neg: Int, NoPrefix, unit)
      case None =>
        throw new Exception(s"Couldn't parse unit of measure $s at offset $pos")
    }
  }
  
  private def parseExponent(s: String, pos: Int, neg: Int, prefix: Prefix, unit: Unit): (Term, Int) = {
    var a = pos
    var b = pos
    val needExp = if (b < s.length && s.charAt(b) == '^') { a += 1; b += 1; true} else false
    while (b < s.length && (s.charAt(b) == '-' || Character.isDigit(s.charAt(b)))) b += 1
    if (needExp && b <= pos) throw new Exception(s"Expected exponent at offset $b")
    if (b > pos) (Term(prefix, unit, s.substring(a, b).toInt * neg), b)
    else (Term(prefix, unit, neg), a)
  }
} 


