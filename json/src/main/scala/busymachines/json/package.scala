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
package busymachines

/**
  * The reason we break the modularity of circe is rather pragmatic. The design philosophy of this
  * library is to have one import for the most common use case. And that is making it easy to encode/decode
  * to and from REST api and, or various database drivers.
  *
  * The ideal is that if you just want to be able to use transform a bunch of case classes into
  * JSON you should really just have to import
  * {{{
  *   import busymachines.json._
  *   import busymachines.json.autoderive._
  * }}}
  *
  * If you also need explicit parsing/pretty printing, or other operations, then it should be realizeable with
  * and additional:
  * {{{
  *   import busymachines.syntax._
  * }}}
  *
  * This is all fine, as long as the basic types [[io.circe.Encoder]] and [[io.circe.Decoder]] keep their
  * current form
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
package object json extends JsonTypeDefinitions with DefaultTypeDiscriminatorConfig {}
