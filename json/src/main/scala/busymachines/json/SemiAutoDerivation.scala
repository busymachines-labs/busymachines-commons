/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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

  final def decoder[A](implicit decode: Lazy[decoding.ConfiguredDecoder[A]]): Decoder[A] =
    circeSemiAuto.deriveDecoder[A](decode)

  final def encoder[A](implicit encode: Lazy[encoding.ConfiguredAsObjectEncoder[A]]): ObjectEncoder[A] =
    circeSemiAuto.deriveEncoder[A](encode)

  final def codec[A](
    implicit
    encode: Lazy[encoding.ConfiguredAsObjectEncoder[A]],
    decode: Lazy[decoding.ConfiguredDecoder[A]],
  ): Codec[A] = Codec.instance[A](encode.value, decode.value)

  final def deriveFor[A]: DerivationHelper[A] =
    circeSemiAuto.deriveFor[A]

  final def enumerationDecoder[A](implicit decode: Lazy[decoding.EnumerationDecoder[A]]): Decoder[A] =
    circeSemiAuto.deriveEnumerationDecoder[A]

  final def enumerationEncoder[A](implicit encode: Lazy[encoding.EnumerationEncoder[A]]): Encoder[A] =
    circeSemiAuto.deriveEnumerationEncoder[A]

  final def enumerationCodec[A](
    implicit
    encode: Lazy[encoding.EnumerationEncoder[A]],
    decode: Lazy[decoding.EnumerationDecoder[A]],
  ): Codec[A] = Codec.instance[A](encode.value, decode.value)
}
