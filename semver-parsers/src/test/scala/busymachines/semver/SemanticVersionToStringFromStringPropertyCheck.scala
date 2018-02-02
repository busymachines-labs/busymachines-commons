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

import org.scalatest._
import org.scalatest.prop.PropertyChecks

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
class SemanticVersionToStringFromStringPropertyCheck
    extends FunSpec with PropertyChecks with SemanticVersionGenerators {

  describe("Label parser") {
    it("should be able to parse all lowercase representations of Label") {
      forAll(labelGenerator) { original: Label =>
        val parsed = SemanticVersionParsers.unsafeParseLabel(original.lowercase)
        assert(parsed == original)
      }
    }

    it("should be able to parse all lowercaseWithDot representations of Label") {
      forAll(labelGenerator) { original: Label =>
        val parsed = SemanticVersionParsers.unsafeParseLabel(original.lowercaseWithDots)
        assert(parsed == original)
      }
    }

    it("should be able to parse all uppercase representations of Label") {
      forAll(labelGenerator) { original: Label =>
        val parsed = SemanticVersionParsers.unsafeParseLabel(original.uppercase)
        assert(parsed == original)
      }
    }

    it("should be able to parse all uppercaseWithDot representations of Label") {
      forAll(labelGenerator) { original: Label =>
        val parsed = SemanticVersionParsers.unsafeParseLabel(original.uppercaseWithDots)
        assert(parsed == original)
      }
    }
  }

  describe("SemanticVersion parser") {
    it("should be able to parse all lowercase representations of SemVer") {
      forAll(semanticVersionGenerator) { original: SemanticVersion =>
        val parsed = SemanticVersionParsers.unsafeParseSemanticVersion(original.lowercase)
        assert(parsed == original)
      }
    }

    it("should be able to parse all lowercaseWithDots representations of SemVer") {
      forAll(semanticVersionGenerator) { original: SemanticVersion =>
        val parsed = SemanticVersionParsers.unsafeParseSemanticVersion(original.lowercaseWithDots)
        assert(parsed == original)
      }
    }

    it("should be able to parse all uppercase representations of SemVer") {
      forAll(semanticVersionGenerator) { original: SemanticVersion =>
        val parsed = SemanticVersionParsers.unsafeParseSemanticVersion(original.uppercase)
        assert(parsed == original)
      }
    }

    it("should be able to parse all uppercaseWithDots representations of SemVer") {
      forAll(semanticVersionGenerator) { original: SemanticVersion =>
        val parsed = SemanticVersionParsers.unsafeParseSemanticVersion(original.uppercaseWithDots)
        assert(parsed == original)
      }
    }
  }

}
