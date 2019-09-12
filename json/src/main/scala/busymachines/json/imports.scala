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
  * This file exists for two reasons:
  * 1) - didn't want to have the library depend too much on package objects
  * 2) - out of the blue we started getting the following compiler error in client code
  *
  * {{{
  *   Error:(20, 11) package cats contains object and package with same name: implicits
  *    one of them needs to be removed from classpath
  *    c.as[String].map(FailureID.apply)
  * }}}
  *
  * Therefore "package-like" imports are modeled as much as possible as static imports of
  * the properties in a simple object
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
object syntax extends JsonSyntax.Implicits

object derive extends SemiAutoDerivation

object autoderive extends io.circe.generic.extras.AutoDerivation
