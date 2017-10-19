package busymachines.json_test.semiauto

import busymachines.json_test._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
/**
  *
  */
private[json_test] object melonsDefaultSemiAutoDecoders {

  import busymachines.json._

  implicit val tasteDecoder: Decoder[Taste] = semiauto.deriveEnumerationDecoder[Taste]
  implicit val melonDecoder: Decoder[Melon] = semiauto.deriveDecoder[Melon]
  implicit val anarchistMelonDecoder: Decoder[AnarchistMelon] = semiauto.deriveDecoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoEncoders {

  import busymachines.json._

  implicit val tasteEncoder: Encoder[Taste] = semiauto.deriveEnumerationEncoder[Taste]
  implicit val melonEncoder: ObjectEncoder[Melon] = semiauto.deriveEncoder[Melon]
  implicit val anarchistMelonEncoder: Encoder[AnarchistMelon] = semiauto.deriveEncoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoCodecs {

  import busymachines.json._

  implicit val tasteEncoder: Codec[Taste] = semiauto.deriveEnumerationCodec[Taste]
  implicit val melonEncoder: Codec[Melon] = semiauto.deriveCodec[Melon]
  implicit val anarchistMelonEncoder: Codec[AnarchistMelon] = semiauto.deriveCodec[AnarchistMelon]

}