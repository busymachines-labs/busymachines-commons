package com.busymachines.commons

import org.scalatest._
import com.busymachines.commons.domain.UnitOfMeasure
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UnitOfMeasureTests extends FlatSpec with Matchers {

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
    assert(UnitOfMeasure("m/s m").normalized === UnitOfMeasure("m2/s"))
    assert(UnitOfMeasure(Metre, Metre).normalized.symbol === "m2")
    assert(UnitOfMeasure("Mg m2s-3s s").normalized.symbol === "Mg m2s-1")
  }
  
  it should "be normalized to baseUnits correctly" in {
    assert(UnitOfMeasure("m/s m").baseUnitNormalized === UnitOfMeasure(Metre^2, Second^-1))
    assert(UnitOfMeasure("W").baseUnitNormalized === UnitOfMeasure(Kilo::Gram, Metre^2, Second^-3))
    assert(UnitOfMeasure(Watt).baseUnitNormalized === UnitOfMeasure(Kilo::Gram, Metre^2, Second^-3))
    assert(UnitOfMeasure(Kilo::Watt).baseUnitNormalized === UnitOfMeasure(Mega::Gram, Metre^2, Second^-3))
    assert(UnitOfMeasure(Kilo::Watt, Hour).baseUnitNormalized === UnitOfMeasure(Mega::Gram, Metre^2, Second^-2))
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
    assert(areCompatible(UnitOfMeasure("kW h"), UnitOfMeasure("hkW")))
    assert(areCompatible(UnitOfMeasure("kW h"), UnitOfMeasure("h W")))
    assert(!areCompatible(UnitOfMeasure("kW h"), UnitOfMeasure("kW")))
  }
  
  "values" should "be converted correctly" in {
    assert(Watt.baseUnitNormalized.withoutPrefix === (Kilo::Watt).baseUnitNormalized.withoutPrefix)
    converter(Hour, Minute)(1) should be (60d +- 1e-5)
    converter(Hour, Second)(1) should be (3600d +- 1e-5)
    // TODO fix this test
//    converter(Hour * Hour, Second * Second)(1) should be (3600d * 3600d +- 1e-5)
    converter(Kilo::Watt, Watt)(200) should be (200000d +- 1e-5)
    converter(Watt, Kilo::Watt)(2000) should be (2d +- 1e-5)
    converter((Kilo::Watt) * Hour, Joule)(1) should be (3600000d +- 1e-5)
    converter(Joule, (Kilo::Watt) * Hour)(3600000) should be (1d +- 1e-5)
    converter((Kilo::Watt) * Hour / Hour, Watt)(1) should be (1000d +- 1e-5)
  }
}