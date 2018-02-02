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
package busymachines.json_test.derive

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

  implicit val tasteDecoder:          Decoder[Taste]          = derive.enumerationDecoder[Taste]
  implicit val melonDecoder:          Decoder[Melon]          = derive.decoder[Melon]
  implicit val anarchistMelonDecoder: Decoder[AnarchistMelon] = derive.decoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoEncoders {

  import busymachines.json._

  implicit val tasteEncoder:          Encoder[Taste]          = derive.enumerationEncoder[Taste]
  implicit val melonEncoder:          ObjectEncoder[Melon]    = derive.encoder[Melon]
  implicit val anarchistMelonEncoder: Encoder[AnarchistMelon] = derive.encoder[AnarchistMelon]
}

/**
  *
  */
private[json_test] object melonsDefaultSemiAutoCodecs {

  import busymachines.json._

  implicit val tasteCodec:          Codec[Taste]          = derive.enumerationCodec[Taste]
  implicit val melonCodec:          Codec[Melon]          = derive.codec[Melon]
  implicit val anarchistMelonCodec: Codec[AnarchistMelon] = derive.codec[AnarchistMelon]

}
