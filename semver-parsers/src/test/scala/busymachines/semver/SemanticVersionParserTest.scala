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

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
class SemanticVersionParserTest extends FlatSpec with Matchers {

  behavior of "SemanticVersion parsers"

  it should "parse: 1.20.345" in {
    val input = "1.20.345"
    assert(parse(input) == SemanticVersion(1, 20, 345))
  }

  it should "parse: 1.20.345-snapshot" in {
    val input = "1.20.345-snapshot"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.snapshot))
  }

  it should "parse: 1.20.345-SNAPSHOT" in {
    val input = "1.20.345-SNAPSHOT"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.snapshot))
  }

  //===========================================================================

  it should "parse: 1.20.345-alpha" in {
    val input = "1.20.345-alpha"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha))
  }

  it should "parse: 1.20.345-ALPHA" in {
    val input = "1.20.345-ALPHA"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha))
  }

  it should "parse: 1.20.345-alpha.1" in {
    val input = "1.20.345-alpha.1"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha(1)))
  }

  it should "parse: 1.20.345-ALPHA.2" in {
    val input = "1.20.345-ALPHA.2"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha(2)))
  }

  it should "parse: 1.20.345-alpha3" in {
    val input = "1.20.345-alpha3"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha(3)))
  }

  it should "parse: 1.20.345-ALPHA4" in {
    val input = "1.20.345-ALPHA4"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.alpha(4)))
  }

  //===========================================================================

  it should "parse: 1.20.345-beta" in {
    val input = "1.20.345-beta"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta))
  }

  it should "parse: 1.20.345-BETA" in {
    val input = "1.20.345-BETA"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta))
  }

  it should "parse: 1.20.345-beta.1" in {
    val input = "1.20.345-beta.1"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta(1)))
  }

  it should "parse: 1.20.345-BETA.2" in {
    val input = "1.20.345-BETA.2"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta(2)))
  }

  it should "parse: 1.20.345-beta3" in {
    val input = "1.20.345-beta3"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta(3)))
  }

  it should "parse: 1.20.345-BETA4" in {
    val input = "1.20.345-BETA4"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.beta(4)))
  }

  //===========================================================================

  it should "parse: 1.20.345-m.1" in {
    val input = "1.20.345-m.1"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.m(1)))
  }

  it should "parse: 1.20.345-M.2" in {
    val input = "1.20.345-M.2"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.m(2)))
  }

  it should "parse: 1.20.345-m3" in {
    val input = "1.20.345-m3"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.m(3)))
  }

  it should "parse: 1.20.345-M4" in {
    val input = "1.20.345-M4"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.m(4)))
  }

  //===========================================================================

  it should "parse: 1.20.345-rc.1" in {
    val input = "1.20.345-rc.1"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.rc(1)))
  }

  it should "parse: 1.20.345-RC.2" in {
    val input = "1.20.345-RC.2"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.rc(2)))
  }

  it should "parse: 1.20.345-rc3" in {
    val input = "1.20.345-rc3"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.rc(3)))
  }

  it should "parse: 1.20.345-RC4" in {
    val input = "1.20.345-RC4"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.rc(4)))
  }

  //===========================================================================

  it should "parse: 1.20.345-snapshot+1234124" in {
    val input = "1.20.345-snapshot+1234124"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.snapshot, Option("1234124")))
  }

  it should "parse: 1.20.345-SNAPSHOT+1234124" in {
    val input = "1.20.345-SNAPSHOT+1234124"
    assert(parse(input) == SemanticVersion(1, 20, 345, Labels.snapshot, Option("1234124")))
  }

  it should "parse: 1.20.345+1234124" in {
    val input = "1.20.345+1234124"
    assert(parse(input) == SemanticVersion(1, 20, 345, None, Option("1234124")))
  }

  //FIXME: is this correct???
  it should "parse: 1.20.345+ — empty meta as empty string" in {
    val input = "1.20.345+"
    assert(parse(input) == SemanticVersion(1, 20, 345, None, Option("")))
  }

  //FIXME: is this correct???
  it should "parse: 1.20.345+asfsd sfddsf — meta with spaces" in {
    val input = "1.20.345+asfsd sfddsf"
    assert(parse(input) == SemanticVersion(1, 20, 345, None, Option("asfsd sfddsf")))
  }

  //===========================================================================

  it should "parse: 1.20.345 — fail on invalid input" in {
    val input = "1.20.345asdf"
    failOn(input)
  }

  //===========================================================================

  def parse(i: String): SemanticVersion = {
    SemanticVersionParsers.unsafeParseSemanticVersion(i)
  }

  def failOn(i: String): InvalidSemanticVersionFailure = {
    the[InvalidSemanticVersionFailure] thrownBy {
      SemanticVersionParsers.unsafeParseSemanticVersion(i)
    }
  }
}
