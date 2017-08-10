package busymachines.json


import io.circe.generic.extras.decoding
import io.circe.generic.extras.encoding
import io.circe.generic.extras.{semiauto => circeSemiAuto}
import shapeless.Lazy

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
trait SemiAutoDerivation {
  type DerivationHelper[A] = io.circe.generic.extras.semiauto.DerivationHelper[A]

  final def deriveDecoder[A](implicit decode: Lazy[decoding.ConfiguredDecoder[A]]): Decoder[A] =
    circeSemiAuto.deriveDecoder[A](decode)

  final def deriveEncoder[A](implicit encode: Lazy[encoding.ConfiguredObjectEncoder[A]]): ObjectEncoder[A] =
    circeSemiAuto.deriveEncoder[A](encode)

  final def deriveCodec[A](implicit
    encode: Lazy[encoding.ConfiguredObjectEncoder[A]],
    decode: Lazy[decoding.ConfiguredDecoder[A]]): Codec[A] = {
    Codec[A](encode.value, decode.value)
  }

  final def deriveFor[A]: DerivationHelper[A] =
    circeSemiAuto.deriveFor[A]

  def deriveEnumerationDecoder[A](implicit decode: Lazy[decoding.EnumerationDecoder[A]]): Decoder[A] =
    circeSemiAuto.deriveEnumerationDecoder[A]

  def deriveEnumerationEncoder[A](implicit encode: Lazy[encoding.EnumerationEncoder[A]]): Encoder[A] =
    circeSemiAuto.deriveEnumerationEncoder[A]

  final def deriveEnumerationCodec[A](implicit
    encode: Lazy[encoding.EnumerationEncoder[A]],
    decode: Lazy[decoding.EnumerationDecoder[A]]
  ): Codec[A] = Codec[A](encode.value, decode.value)
}
