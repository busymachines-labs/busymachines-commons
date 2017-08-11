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
  import busymachines.json.semiauto._

  implicit val tasteDecoder: Decoder[Taste] = deriveEnumerationDecoder[Taste]
  implicit val melonDecoder: Decoder[Melon] = deriveDecoder[Melon]
  implicit val anarchistMelonDecoder: Decoder[AnarchistMelon] = deriveDecoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoEncoders {

  import busymachines.json._
  import busymachines.json.semiauto._

  implicit val tasteEncoder: Encoder[Taste] = deriveEnumerationEncoder[Taste]
  implicit val melonEncoder: ObjectEncoder[Melon] = deriveEncoder[Melon]
  implicit val anarchistMelonEncoder: Encoder[AnarchistMelon] = deriveEncoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoCodecs {

  import busymachines.json._
  import busymachines.json.semiauto._

  implicit val tasteEncoder: Codec[Taste] = deriveEnumerationCodec[Taste]
  implicit val melonEncoder: Codec[Melon] = deriveCodec[Melon]
  implicit val anarchistMelonEncoder: Codec[AnarchistMelon] = deriveCodec[AnarchistMelon]

}