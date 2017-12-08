package busymachines.semver

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
class SemanticVersionShowsTest extends FlatSpec with Matchers {

  val snapshot    = SemanticVersion(1, 0, 0, Labels.snapshot)
  val alphaSingle = SemanticVersion(1, 0, 0, Labels.alpha)
  val alpha1      = SemanticVersion(1, 0, 0, Labels.alpha(1))
  val betaSingle  = SemanticVersion(1, 0, 0, Labels.beta)
  val beta1       = SemanticVersion(1, 0, 0, Labels.beta(1))
  val m1          = SemanticVersion(1, 0, 0, Labels.m(1))
  val rc1         = SemanticVersion(1, 0, 0, Labels.rc(1))

  val snapshotWMeta    = SemanticVersion(1, 0, 0, Labels.snapshot, Option("1111"))
  val alphaSingleWMeta = SemanticVersion(1, 0, 0, Labels.alpha,    Option("2222"))
  val alpha1WMeta      = SemanticVersion(1, 0, 0, Labels.alpha(1), Option("3333"))
  val betaSingleWMeta  = SemanticVersion(1, 0, 0, Labels.beta,     Option("4444"))
  val beta1WMeta       = SemanticVersion(1, 0, 0, Labels.beta(1),  Option("5555"))
  val m1WMeta          = SemanticVersion(1, 0, 0, Labels.m(1),     Option("6666"))
  val rc1WMeta         = SemanticVersion(1, 0, 0, Labels.rc(1),    Option("7777"))

  val snapshotWOLabelWMeta    = SemanticVersion(1, 0, 0, None, Option("1111"))
  val alphaSingleWOLabelWMeta = SemanticVersion(1, 0, 0, None, Option("2222"))
  val alpha1WOLabelWMeta      = SemanticVersion(1, 0, 0, None, Option("3333"))
  val betaSingleWOLabelWMeta  = SemanticVersion(1, 0, 0, None, Option("4444"))
  val beta1WOLabelWMeta       = SemanticVersion(1, 0, 0, None, Option("5555"))
  val m1WOLabelWMeta          = SemanticVersion(1, 0, 0, None, Option("6666"))
  val rc1WOLabelWMeta         = SemanticVersion(1, 0, 0, None, Option("7777"))

  behavior of "lowercase, no separator show"

  it should "properly display with w/o label, w/o meta" in {
    val v1 = SemanticVersion(1, 0, 0)
    assert(v1.lowercase == "1.0.0")
  }

  it should "properly display with w/ label, no meta" in {
    assert(snapshot.lowercase == "1.0.0-snapshot")
    assert(alphaSingle.lowercase == "1.0.0-alpha")
    assert(alpha1.lowercase == "1.0.0-alpha1")
    assert(betaSingle.lowercase == "1.0.0-beta")
    assert(beta1.lowercase == "1.0.0-beta1")
    assert(m1.lowercase == "1.0.0-m1")
    assert(rc1.lowercase == "1.0.0-rc1")
  }

  it should "properly display with w/ label, w/ meta" in {
    assert(snapshotWMeta.lowercase == "1.0.0-snapshot+1111")
    assert(alphaSingleWMeta.lowercase == "1.0.0-alpha+2222")
    assert(alpha1WMeta.lowercase == "1.0.0-alpha1+3333")
    assert(betaSingleWMeta.lowercase == "1.0.0-beta+4444")
    assert(beta1WMeta.lowercase == "1.0.0-beta1+5555")
    assert(m1WMeta.lowercase == "1.0.0-m1+6666")
    assert(rc1WMeta.lowercase == "1.0.0-rc1+7777")
  }

  it should "properly display with w/o label, w/ meta" in {
    assert(snapshotWOLabelWMeta.lowercase == "1.0.0+1111")
    assert(alphaSingleWOLabelWMeta.lowercase == "1.0.0+2222")
    assert(alpha1WOLabelWMeta.lowercase == "1.0.0+3333")
    assert(betaSingleWOLabelWMeta.lowercase == "1.0.0+4444")
    assert(beta1WOLabelWMeta.lowercase == "1.0.0+5555")
    assert(m1WOLabelWMeta.lowercase == "1.0.0+6666")
    assert(rc1WOLabelWMeta.lowercase == "1.0.0+7777")
  }

  behavior of "lowercase, dot separator show"

  it should "properly display with w/o label, w/o meta" in {
    val v1 = SemanticVersion(1, 0, 0)
    assert(v1.lowercaseWithDots == "1.0.0")
  }

  it should "properly display with w/ label, no meta" in {
    assert(snapshot.lowercaseWithDots == "1.0.0-snapshot")
    assert(alphaSingle.lowercaseWithDots == "1.0.0-alpha")
    assert(alpha1.lowercaseWithDots == "1.0.0-alpha.1")
    assert(betaSingle.lowercaseWithDots == "1.0.0-beta")
    assert(beta1.lowercaseWithDots == "1.0.0-beta.1")
    assert(m1.lowercaseWithDots == "1.0.0-m.1")
    assert(rc1.lowercaseWithDots == "1.0.0-rc.1")
  }

  it should "properly display with w/ label, w/ meta" in {
    assert(snapshotWMeta.lowercaseWithDots == "1.0.0-snapshot+1111")
    assert(alphaSingleWMeta.lowercaseWithDots == "1.0.0-alpha+2222")
    assert(alpha1WMeta.lowercaseWithDots == "1.0.0-alpha.1+3333")
    assert(betaSingleWMeta.lowercaseWithDots == "1.0.0-beta+4444")
    assert(beta1WMeta.lowercaseWithDots == "1.0.0-beta.1+5555")
    assert(m1WMeta.lowercaseWithDots == "1.0.0-m.1+6666")
    assert(rc1WMeta.lowercaseWithDots == "1.0.0-rc.1+7777")
  }

  it should "properly display with w/o label, w/ meta" in {
    assert(snapshotWOLabelWMeta.lowercaseWithDots == "1.0.0+1111")
    assert(alphaSingleWOLabelWMeta.lowercaseWithDots == "1.0.0+2222")
    assert(alpha1WOLabelWMeta.lowercaseWithDots == "1.0.0+3333")
    assert(betaSingleWOLabelWMeta.lowercaseWithDots == "1.0.0+4444")
    assert(beta1WOLabelWMeta.lowercaseWithDots == "1.0.0+5555")
    assert(m1WOLabelWMeta.lowercaseWithDots == "1.0.0+6666")
    assert(rc1WOLabelWMeta.lowercaseWithDots == "1.0.0+7777")
  }

  behavior of "uppercase, no separator show"

  it should "properly display with w/o label, w/o meta" in {
    val v1 = SemanticVersion(1, 0, 0)
    assert(v1.uppercase == "1.0.0")
  }

  it should "properly display with w/ label, no meta" in {
    assert(snapshot.uppercase == "1.0.0-SNAPSHOT")
    assert(alphaSingle.uppercase == "1.0.0-ALPHA")
    assert(alpha1.uppercase == "1.0.0-ALPHA1")
    assert(betaSingle.uppercase == "1.0.0-BETA")
    assert(beta1.uppercase == "1.0.0-BETA1")
    assert(m1.uppercase == "1.0.0-M1")
    assert(rc1.uppercase == "1.0.0-RC1")
  }

  it should "properly display with w/ label, w/ meta" in {
    assert(snapshotWMeta.uppercase == "1.0.0-SNAPSHOT+1111")
    assert(alphaSingleWMeta.uppercase == "1.0.0-ALPHA+2222")
    assert(alpha1WMeta.uppercase == "1.0.0-ALPHA1+3333")
    assert(betaSingleWMeta.uppercase == "1.0.0-BETA+4444")
    assert(beta1WMeta.uppercase == "1.0.0-BETA1+5555")
    assert(m1WMeta.uppercase == "1.0.0-M1+6666")
    assert(rc1WMeta.uppercase == "1.0.0-RC1+7777")
  }

  it should "properly display with w/o label, w/ meta" in {
    assert(snapshotWOLabelWMeta.uppercase == "1.0.0+1111")
    assert(alphaSingleWOLabelWMeta.uppercase == "1.0.0+2222")
    assert(alpha1WOLabelWMeta.uppercase == "1.0.0+3333")
    assert(betaSingleWOLabelWMeta.uppercase == "1.0.0+4444")
    assert(beta1WOLabelWMeta.uppercase == "1.0.0+5555")
    assert(m1WOLabelWMeta.uppercase == "1.0.0+6666")
    assert(rc1WOLabelWMeta.uppercase == "1.0.0+7777")
  }

  behavior of "uppercase, dot separator show"

  it should "properly display with w/o label, w/o meta" in {
    val v1 = SemanticVersion(1, 0, 0)
    assert(v1.uppercaseWithDots == "1.0.0")
  }

  it should "properly display with w/ label, no meta" in {
    assert(snapshot.uppercaseWithDots == "1.0.0-SNAPSHOT")
    assert(alphaSingle.uppercaseWithDots == "1.0.0-ALPHA")
    assert(alpha1.uppercaseWithDots == "1.0.0-ALPHA.1")
    assert(betaSingle.uppercaseWithDots == "1.0.0-BETA")
    assert(beta1.uppercaseWithDots == "1.0.0-BETA.1")
    assert(m1.uppercaseWithDots == "1.0.0-M.1")
    assert(rc1.uppercaseWithDots == "1.0.0-RC.1")
  }

  it should "properly display with w/ label, w/ meta" in {
    assert(snapshotWMeta.uppercaseWithDots == "1.0.0-SNAPSHOT+1111")
    assert(alphaSingleWMeta.uppercaseWithDots == "1.0.0-ALPHA+2222")
    assert(alpha1WMeta.uppercaseWithDots == "1.0.0-ALPHA.1+3333")
    assert(betaSingleWMeta.uppercaseWithDots == "1.0.0-BETA+4444")
    assert(beta1WMeta.uppercaseWithDots == "1.0.0-BETA.1+5555")
    assert(m1WMeta.uppercaseWithDots == "1.0.0-M.1+6666")
    assert(rc1WMeta.uppercaseWithDots == "1.0.0-RC.1+7777")
  }

  it should "properly display with w/o label, w/ meta" in {
    assert(snapshotWOLabelWMeta.uppercaseWithDots == "1.0.0+1111")
    assert(alphaSingleWOLabelWMeta.uppercaseWithDots == "1.0.0+2222")
    assert(alpha1WOLabelWMeta.uppercaseWithDots == "1.0.0+3333")
    assert(betaSingleWOLabelWMeta.uppercaseWithDots == "1.0.0+4444")
    assert(beta1WOLabelWMeta.uppercaseWithDots == "1.0.0+5555")
    assert(m1WOLabelWMeta.uppercaseWithDots == "1.0.0+6666")
    assert(rc1WOLabelWMeta.uppercaseWithDots == "1.0.0+7777")
  }
}
