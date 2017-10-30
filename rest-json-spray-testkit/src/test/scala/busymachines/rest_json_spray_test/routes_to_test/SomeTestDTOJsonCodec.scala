package busymachines.rest_json_spray_test.routes_to_test

import busymachines.json.BusymachinesDefaultJsonCodec

/**
  *
  * Defining JSON encoders like this greatly increases compilation speed, and you only
  * have to derive the top-most types anyway. Nested types of [[SomeTestDTOPost]], etc.
  * are still derived automatically.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
private[rest_json_spray_test] object SomeTestDTOJsonCodec extends SomeTestDTOJsonCodec

private[rest_json_spray_test] trait SomeTestDTOJsonCodec extends BusymachinesDefaultJsonCodec {

  import busymachines.json._

  implicit val someTestDTOGetCodec: Codec[SomeTestDTOGet] = derive.jsonFormat3(SomeTestDTOGet)
  implicit val someTestDTOPostCodec: Codec[SomeTestDTOPost] = derive.jsonFormat2(SomeTestDTOPost)
  implicit val someTestDTOPutCodec: Codec[SomeTestDTOPut] = derive.jsonFormat2(SomeTestDTOPut)
  implicit val someTestDTOPatchCodec: Codec[SomeTestDTOPatch] = derive.jsonFormat1(SomeTestDTOPatch)

}
