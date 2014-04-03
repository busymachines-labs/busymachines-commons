package com.busymachines.commons

import org.scalatest.FlatSpec
import com.busymachines.commons.domain.UnitOfMeasure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UnitOfMeasureTests extends FlatSpec {

  import UnitOfMeasure._
  
  "symbols" should "be parsed correctly" in {
    assert(UnitOfMeasure("m/sm") == UnitOfMeasure(Metre, Second^-1, Metre))
    assert(UnitOfMeasure("m/s m") == UnitOfMeasure(Metre, Second^-1, Metre))
    assert(UnitOfMeasure("m/s m2") == UnitOfMeasure(Metre, Second^-1, Metre^2))
    assert(UnitOfMeasure("m/s m^2") == UnitOfMeasure(Metre, Second^-1, Metre^2))
    assert(UnitOfMeasure("hW") == UnitOfMeasure(Hecto::Watt))
    assert(UnitOfMeasure("h W") == UnitOfMeasure(Hour, Watt))
  }
    
  "units" should "be normalized correctly" in {
    assert(UnitOfMeasure("m/s m").normalized === UnitOfMeasure(Metre^2, Second^-1))
    assert(UnitOfMeasure(Metre, Metre).normalized.symbol === "m2")
  }
  
  it should "be normalized to baseUnits correctly" in {
    assert(UnitOfMeasure("m/s m").baseUnitNormalized === UnitOfMeasure(Metre^2, Second^-1))
    assert(UnitOfMeasure("W").baseUnitNormalized === UnitOfMeasure(Kilo::Gram, Metre^2, Second^-3))
    assert(UnitOfMeasure(Watt).baseUnitNormalized === UnitOfMeasure(Kilo::Gram, Metre^2, Second^-3))
    assert(UnitOfMeasure(Kilo::Watt).baseUnitNormalized === UnitOfMeasure(Mega::Gram, Metre^2, Second^-3))
  }
  
  it should "be printed without separator when possible" in {
    assert(UnitOfMeasure(Kilo::Watt, Hour).symbol === "kWh")
    assert(UnitOfMeasure(Metre, Metre).symbol === "m m")
    assert(Watt.symbol === "W")
    assert(UnitOfMeasure(Metre, Metre).normalized.symbol === "m2")
    assert(UnitOfMeasure(Hour, Watt).symbol === "h W")
    assert(UnitOfMeasure(Hecto::Watt).symbol === "hW")
  }
  
  it should "correctly test for compatibility" in {
    assert(UnitOfMeasure("kW h").isCompatibleWith(UnitOfMeasure("hkW")))
    assert(UnitOfMeasure("kW h").isCompatibleWith(UnitOfMeasure("h W")))
    assert(!UnitOfMeasure("kW h").isCompatibleWith(UnitOfMeasure("kW")))
  }
  
  "values" should "be converted correctly" in {
    assert(Watt.baseUnitNormalized.withoutPrefix === (Kilo::Watt).baseUnitNormalized.withoutPrefix)
    assert(Watt.convert(200, Kilo::Watt) === 200000)
    assert((Kilo::Watt).convert(2000, Watt) === 2)
    assert(Joule.convert(1, (Kilo::Watt, Hour)) === 3600000)
  }
}