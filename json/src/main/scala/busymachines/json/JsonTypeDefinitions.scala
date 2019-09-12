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

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait JsonTypeDefinitions {
  final type Encoder[A]       = io.circe.Encoder[A]
  final type ObjectEncoder[A] = io.circe.Encoder.AsObject[A]

  @inline final def Encoder:       io.circe.Encoder.type          = io.circe.Encoder
  @inline final def ObjectEncoder: io.circe.Encoder.AsObject.type = io.circe.Encoder.AsObject

  final type Decoder[A]       = io.circe.Decoder[A]
  final type ObjectDecoder[A] = io.circe.Encoder.AsObject[A]

  @inline final def Decoder:       io.circe.Decoder.type          = io.circe.Decoder
  @inline final def ObjectDecoder: io.circe.Encoder.AsObject.type = io.circe.Encoder.AsObject

  final type Configuration = io.circe.generic.extras.Configuration
  @inline final def Configuration: io.circe.generic.extras.Configuration.type = io.circe.generic.extras.Configuration

  final type Json       = io.circe.Json
  final type JsonObject = io.circe.JsonObject

  @inline final def Json:       io.circe.Json.type       = io.circe.Json
  @inline final def JsonObject: io.circe.JsonObject.type = io.circe.JsonObject

  final type HCursor = io.circe.HCursor
  @inline final def HCursor: io.circe.HCursor.type = io.circe.HCursor
}
