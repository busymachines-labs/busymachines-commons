package busymachines.semver

import org.scalatest.{Assertion, FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Nov 2017
  *
  */
class SemanticVersionOrderingTest extends FlatSpec with Matchers {

  //technically I could have used >, ==, < operators as well
  private implicit class SemVerTestOps(sv: SemanticVersion) {

    def assertGT(that: SemanticVersion): Assertion = {
      assert(sv.compareTo(that) > 0, s"$sv should be greater than $that")
    }

    def assertEQ(that: SemanticVersion): Assertion = {
      assert(sv.compareTo(that) == 0, s"$sv should be equal to $that")
    }

    def assertLT(that: SemanticVersion): Assertion = {
      assert(sv.compareTo(that) < 0, s"$sv should be less than $that")
    }
  }

  behavior of "SemanticVersion ordering"

  it should "be equal w/ label" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.rc(1))

    v1 assertEQ v2
    v2 assertEQ v1
  }

  it should "be equal w/o label" in {
    val v1 = SemanticVersion(1, 0, 0)
    val v2 = SemanticVersion(1, 0, 0)

    v1 assertEQ v2
  }

  it should "properly compare when major dominates w/ label" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(2, 0, 0, Labels.rc(1))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when major dominates w/o label" in {
    val v1 = SemanticVersion(1, 0, 0)
    val v2 = SemanticVersion(2, 0, 0)

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when minor dominates w/ label" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(1, 1, 0, Labels.rc(1))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when minor dominates w/o label" in {
    val v1 = SemanticVersion(1, 0, 0)
    val v2 = SemanticVersion(1, 1, 0)

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when patch dominates w/ label" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(1, 0, 1, Labels.rc(1))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when patch dominates w/o label" in {
    val v1 = SemanticVersion(1, 0, 0)
    val v2 = SemanticVersion(1, 0, 1)

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — snapshot < alpha < beta < RC < M" in {
    val snapshot = SemanticVersion(1, 0, 0, Labels.snapshot)
    val alpha    = SemanticVersion(1, 0, 0, Labels.alpha(4))
    val beta     = SemanticVersion(1, 0, 0, Labels.beta(3))
    val m        = SemanticVersion(1, 0, 0, Labels.m(2))
    val rc       = SemanticVersion(1, 0, 0, Labels.rc(1))

    withClue("snapshot") {
      snapshot assertEQ snapshot
      snapshot assertLT alpha
      snapshot assertLT beta
      snapshot assertLT m
      snapshot assertLT rc

      snapshot assertEQ snapshot
      alpha assertGT snapshot
      beta assertGT snapshot
      m assertGT snapshot
      rc assertGT snapshot
    }

    withClue("alpha") {
      alpha assertGT snapshot
      alpha assertEQ alpha
      alpha assertLT beta
      alpha assertLT m
      alpha assertLT rc

      snapshot assertLT alpha
      alpha assertEQ alpha
      beta assertGT alpha
      m assertGT alpha
      rc assertGT alpha
    }

    withClue("beta") {
      beta assertGT snapshot
      beta assertGT alpha
      beta assertEQ beta
      beta assertLT m
      beta assertLT rc

      snapshot assertLT beta
      alpha assertLT beta
      beta assertEQ beta
      m assertGT beta
      rc assertGT beta
    }

    withClue("M") {
      m assertGT snapshot
      m assertGT alpha
      m assertGT beta
      m assertEQ m
      m assertLT rc

      snapshot assertLT m
      alpha assertLT m
      beta assertLT m
      m assertEQ m
      rc assertGT m
    }

    withClue("RC") {
      rc assertGT snapshot
      rc assertGT alpha
      rc assertGT beta
      rc assertGT m
      rc assertEQ rc

      snapshot assertLT rc
      alpha assertLT rc
      beta assertLT rc
      m assertLT rc
      rc assertEQ rc
    }
  }

  it should "properly compare when label dominates — same label — alpha" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.alpha(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.alpha(2))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — alpha — 1 < 10" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.alpha(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.alpha(11))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — beta" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.beta(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.beta(2))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — beta — 1 < 10" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.beta(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.beta(11))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — RC" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.rc(2))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — RC — 1 < 10" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.rc(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.rc(11))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — M" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.m(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.m(2))

    v1 assertLT v2
    v2 assertGT v1
  }

  it should "properly compare when label dominates — same label — M — 1 < 10" in {
    val v1 = SemanticVersion(1, 0, 0, Labels.m(1))
    val v2 = SemanticVersion(1, 0, 0, Labels.m(11))

    v1 assertLT v2
    v2 assertGT v1
  }

}
