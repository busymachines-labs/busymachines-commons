package busymachines.rest_json_test.routes_to_test

import spray.json.DefaultJsonProtocol

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
private[rest_json_test] object SomeTestDTOJsonCodec extends SomeTestDTOJsonCodec

private[rest_json_test] trait SomeTestDTOJsonCodec extends DefaultJsonProtocol {

  import busymachines.json._

  implicit val someTestDTOGetCodec: Codec[SomeTestDTOGet] = ???
  implicit val someTestDTOPostCodec: Codec[SomeTestDTOPost] = ???
  implicit val someTestDTOPutCodec: Codec[SomeTestDTOPut] = ???
  implicit val someTestDTOPatchCodec: Codec[SomeTestDTOPatch] = ???

}
