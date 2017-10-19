package busymachines.rest_test.routes

import busymachines.rest.JsonSupport

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
private[rest_test] object SomeTestDTOJsonCodec extends SomeTestDTOJsonCodec

private[rest_test] trait SomeTestDTOJsonCodec extends JsonSupport {

  import busymachines.json._

  implicit val someTestDTOGetCodec: Codec[SomeTestDTOGet] = derive.codec[SomeTestDTOGet]
  implicit val someTestDTOPostCodec: Codec[SomeTestDTOPost] = derive.codec[SomeTestDTOPost]
  implicit val someTestDTOPutCodec: Codec[SomeTestDTOPut] = derive.codec[SomeTestDTOPut]
  implicit val someTestDTOPatchCodec: Codec[SomeTestDTOPatch] = derive.codec[SomeTestDTOPatch]

}
