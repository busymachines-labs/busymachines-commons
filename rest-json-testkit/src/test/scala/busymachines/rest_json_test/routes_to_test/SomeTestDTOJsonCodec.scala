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
package busymachines.rest_json_test.routes_to_test

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

private[rest_json_test] trait SomeTestDTOJsonCodec {

  import busymachines.json._

  implicit val someTestDTOGetCodec:   Codec[SomeTestDTOGet]   = derive.codec[SomeTestDTOGet]
  implicit val someTestDTOPostCodec:  Codec[SomeTestDTOPost]  = derive.codec[SomeTestDTOPost]
  implicit val someTestDTOPutCodec:   Codec[SomeTestDTOPut]   = derive.codec[SomeTestDTOPut]
  implicit val someTestDTOPatchCodec: Codec[SomeTestDTOPatch] = derive.codec[SomeTestDTOPatch]

}
