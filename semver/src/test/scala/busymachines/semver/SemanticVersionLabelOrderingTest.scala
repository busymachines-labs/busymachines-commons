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

import org.scalatest.{Assertion, FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
class SemanticVersionLabelOrderingTest extends FlatSpec with Matchers {

  //technically I could have used >, ==, < operators as well
  private implicit class LabelTestOps(label: Label) {

    def assertGT(that: Label): Assertion = {
      assert(label.compareTo(that) > 0, s"$label should be greater than $that")
    }

    def assertEQ(that: Label): Assertion = {
      assert(label.compareTo(that) == 0, s"$label should be equal to $that")
    }

    def assertLT(that: Label): Assertion = {
      assert(label.compareTo(that) < 0, s"$label should be less than $that")
    }
  }

  behavior of "semver label snapshot"

  it should "be equal to snapshot label" in {
    val l1 = Labels.snapshot
    val l2 = Labels.snapshot

    l1 assertEQ l2
    l2 assertEQ l1
  }

  behavior of "semver label alpha"

  it should "be the same as Int natural ordering when comparing with other alphas" in {
    withClue("EQ") {
      val l1 = Labels.alpha(1)
      val l2 = Labels.alpha(1)

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.alpha(1)
      val l2 = Labels.alpha(2)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  behavior of "semver label alpha-singleton"

  it should "only equal alpha singleton < alpha" in {
    withClue("EQ") {
      val l1 = Labels.alpha
      val l2 = Labels.alpha

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.alpha
      val l2 = Labels.alpha(1)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  behavior of "semver label beta-singleton"

  it should "only equal beta singleton < beta" in {
    withClue("EQ") {
      val l1 = Labels.beta
      val l2 = Labels.beta

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.beta
      val l2 = Labels.beta(1)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  behavior of "semver label beta"

  it should "be the same as Int natural ordering when comparing with other betas" in {
    withClue("EQ") {
      val l1 = Labels.beta(1)
      val l2 = Labels.beta(1)

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.beta(1)
      val l2 = Labels.beta(2)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  it should "be the same as Int natural ordering when comparing with other Ms" in {
    withClue("EQ") {
      val l1 = Labels.m(1)
      val l2 = Labels.m(1)

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.m(1)
      val l2 = Labels.m(2)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  it should "be the same as Int natural ordering when comparing with other RCs" in {
    withClue("EQ") {
      val l1 = Labels.rc(1)
      val l2 = Labels.rc(1)

      l1 assertEQ l2
      l2 assertEQ l1
    }

    withClue("LT/GT") {
      val l1 = Labels.rc(1)
      val l2 = Labels.rc(2)

      l1 assertLT l2
      l2 assertGT l1
    }
  }

  behavior of "semver label truth table"

  it should "work wrt to all possible combinations of types, and EQ/LT/GT operators" in {
    val snapshotLabel = Labels.snapshot
    val alphaSingle   = Labels.alpha
    val alphaLabel    = Labels.alpha(1)
    val betaSingle    = Labels.beta
    val betaLabel     = Labels.beta(1)
    val mLabel        = Labels.m(1)
    val rcLabel       = Labels.rc(1)

    withClue("snapshot") {
      snapshotLabel assertEQ snapshotLabel
      snapshotLabel assertLT alphaSingle
      snapshotLabel assertLT alphaLabel
      snapshotLabel assertLT betaSingle
      snapshotLabel assertLT betaLabel
      snapshotLabel assertLT mLabel
      snapshotLabel assertLT rcLabel

      snapshotLabel assertEQ snapshotLabel
      alphaSingle assertGT snapshotLabel
      alphaLabel assertGT snapshotLabel
      betaSingle assertGT snapshotLabel
      betaLabel assertGT snapshotLabel
      mLabel assertGT snapshotLabel
      rcLabel assertGT snapshotLabel
    }

    withClue("alpha-single") {
      alphaSingle assertGT snapshotLabel
      alphaSingle assertEQ alphaSingle
      alphaSingle assertLT alphaLabel
      alphaSingle assertLT betaSingle
      alphaSingle assertLT betaLabel
      alphaSingle assertLT mLabel
      alphaSingle assertLT rcLabel

      snapshotLabel assertLT alphaSingle
      alphaSingle assertEQ alphaSingle
      alphaLabel assertGT alphaSingle
      betaSingle assertGT alphaSingle
      betaLabel assertGT alphaSingle
      mLabel assertGT alphaSingle
      rcLabel assertGT alphaSingle
    }

    withClue("alpha") {
      alphaLabel assertGT snapshotLabel
      alphaLabel assertGT alphaSingle
      alphaLabel assertEQ alphaLabel
      alphaLabel assertLT betaSingle
      alphaLabel assertLT betaLabel
      alphaLabel assertLT mLabel
      alphaLabel assertLT rcLabel

      snapshotLabel assertLT alphaLabel
      alphaSingle assertLT alphaLabel
      alphaLabel assertEQ alphaLabel
      betaSingle assertGT alphaLabel
      betaLabel assertGT alphaLabel
      mLabel assertGT alphaLabel
      rcLabel assertGT alphaLabel
    }

    withClue("beta-single") {
      betaSingle assertGT snapshotLabel
      betaSingle assertGT alphaLabel
      betaSingle assertEQ betaSingle
      betaSingle assertLT betaLabel
      betaSingle assertLT mLabel
      betaSingle assertLT rcLabel

      snapshotLabel assertLT betaSingle
      alphaLabel assertLT betaSingle
      betaSingle assertEQ betaSingle
      betaLabel assertGT betaSingle
      mLabel assertGT betaSingle
      rcLabel assertGT betaSingle
    }

    withClue("beta") {
      betaLabel assertGT snapshotLabel
      betaLabel assertGT alphaLabel
      betaLabel assertGT betaSingle
      betaLabel assertEQ betaLabel
      betaLabel assertLT mLabel
      betaLabel assertLT rcLabel

      snapshotLabel assertLT betaLabel
      alphaLabel assertLT betaLabel
      betaSingle assertLT betaLabel
      betaLabel assertEQ betaLabel
      mLabel assertGT betaLabel
      rcLabel assertGT betaLabel
    }

    withClue("M") {
      mLabel assertGT snapshotLabel
      mLabel assertGT alphaSingle
      mLabel assertGT alphaLabel
      mLabel assertGT betaSingle
      mLabel assertGT betaLabel
      mLabel assertEQ mLabel
      mLabel assertLT rcLabel

      snapshotLabel assertLT mLabel
      alphaSingle assertLT mLabel
      alphaLabel assertLT mLabel
      betaSingle assertLT mLabel
      betaLabel assertLT mLabel
      mLabel assertEQ mLabel
      rcLabel assertGT mLabel
    }

    withClue("RC") {
      rcLabel assertGT snapshotLabel
      rcLabel assertGT alphaSingle
      rcLabel assertGT alphaLabel
      rcLabel assertGT betaSingle
      rcLabel assertGT betaLabel
      rcLabel assertGT mLabel
      rcLabel assertEQ rcLabel

      snapshotLabel assertLT rcLabel
      alphaSingle assertLT rcLabel
      alphaLabel assertLT rcLabel
      betaSingle assertLT rcLabel
      betaLabel assertLT rcLabel
      mLabel assertLT rcLabel
      rcLabel assertEQ rcLabel
    }
  }
}
