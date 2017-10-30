package busymachines.json_spray_test.derive_spray_test

import busymachines.json._

import busymachines.json_spray_test._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */

private[json_spray_test] object MelonsJsonCodec extends BusymachinesDefaultJsonCodec {

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