# busymachines-commons-json-spray

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json-spray_2.12)

_*DO NOT DEPEND ON BOTH THIS MODULE AND `json`. They share the same packages. It will end badly, chose one or the other. THIS MODULE WILL RECEIVE WAY LESS ATTENTION THAN THE OTHERS, AND HAS A HIGH CHANCE OF BEING DROPPED*_

Current version is `0.2.0-RC6`. SBT module id:
`"com.busymachines" %% "busymachines-commons-json-spray" % "0.2.0-RC6"`

You should really, really use the [`json`](../json/README.md) module instead. This one is simply a legacy implementation for supporting the now defunct `spray-json`.

## Deriving

Automatic derivation is not available at all. And semi-auto derivation is more spray-like with the `jsonFormatX` pattern.

Look at the tests. Basically, you mixin the `BusymachinesDefaultJsonCodec`, and that bring in scope
and object `derive` which contains all necessary helpers, including lower boilerplate type-class hierarchy derivers with a default `_type` discriminator.
```scala
import busymachines.json._
import busymachines.json_test._

object MelonsJsonCodec extends BusymachinesDefaultJsonCodec {

  implicit val tasteCodec: ValueCodec[Taste] = derive.enumerationCodec[Taste](
    SweetTaste.asConstant("SweetTaste"),
    SourTaste.asConstant("SourTaste")
  )

  implicit val melonCodec: Codec[Melon] = derive.adt[Melon](
    derive.jsonFormat2(WinterMelon).embedTypeField("WinterMelon"),
    derive.jsonFormat2(WaterMelon).embedTypeField("WaterMelon"),
    derive.jsonFormat2(SquareMelon).embedTypeField("SquareMelon"),
    derive.jsonObject(SmallMelon).embedTypeField("SmallMelon")
  )

  implicit val anarchistMelonCodec: Codec[AnarchistMelon] = derive.jsonFormat3(AnarchistMelon)
}
```
