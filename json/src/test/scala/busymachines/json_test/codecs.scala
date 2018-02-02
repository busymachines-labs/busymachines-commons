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
package busymachines.json_test

/**
  *
  * Compile time optimization for JSON codec tests, every bit helps
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
object codecs {
  import busymachines.json._

  val `OutdoorMelons.Color.codec`:            Codec[OutdoorMelons.Color] = derive.codec[OutdoorMelons.Color]
  val `OutdoorMelons.Color.enumerationCodec`: Codec[OutdoorMelons.Color] = derive.enumerationCodec[OutdoorMelons.Color]

}
