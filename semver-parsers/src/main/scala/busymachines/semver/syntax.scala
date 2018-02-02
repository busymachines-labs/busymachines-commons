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
package busymachines.semver

import busymachines.effects.sync._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
object syntax {

  implicit class SemanticVersionCompanionOps(doNotCare: SemanticVersion.type) {

    def fromString(semVer: String): Result[SemanticVersion] =
      SemanticVersionParsers.parseSemanticVersion(semVer)

    def unsafeFromString(semVer: String): SemanticVersion =
      SemanticVersionParsers.unsafeParseSemanticVersion(semVer)
  }

  implicit class SemanticVersionLabelCompanionOps(doNotCare: Labels.type) {

    def fromString(semVer: String): Result[Label] =
      SemanticVersionParsers.parseLabel(semVer)

    def unsafeFromString(label: String): Label =
      SemanticVersionParsers.unsafeParseLabel(label)
  }

}
