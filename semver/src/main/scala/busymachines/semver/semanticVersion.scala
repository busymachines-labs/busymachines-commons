package busymachines.semver

/**
  *
  * A type denoting semantic versions, eg:
  * 4.1.0, with an optional label like "RC", or "beta", etc.
  *
  * @param meta
  * denotes various meta-information, that is included at the end of the version, after the "+" sign.
  * Examples: git sha version:
  * {{{
  *            4.1.0-SNAPSHOT+11223aaff
  * }}}
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Nov 2017
  *
  */
final case class SemanticVersion(
  major: Int,
  minor: Int,
  patch: Int,
  label: Option[Label] = Option.empty[Label],
  meta:  Option[String] = Option.empty[String]
) extends SemanticVersionOrdering with Ordered[SemanticVersion] {

  /**
    * eg:
    * {{{
    *   3.1.2-rc4+1234
    * }}}
    */
  def lowercase: String = SemanticVersionShows.lowercase.noSeparator.show(this)

  /**
    * eg:
    * {{{
    *   3.1.2-rc.4+1234
    * }}}
    */
  def lowercaseWithDots: String = SemanticVersionShows.lowercase.withDotSeparator.show(this)

  /**
    * eg:
    * {{{
    *   3.1.2-RC4+1234
    * }}}
    */
  def uppercase: String = SemanticVersionShows.uppercase.noSeparator.show(this)

  /**
    * eg:
    * {{{
    *   3.1.2-RC.4+1234
    * }}}
    */
  def uppercaseWithDots: String = SemanticVersionShows.uppercase.withDotSeparator.show(this)

  override lazy val toString: String = uppercase
}

sealed trait Label extends Ordered[Label] with LabelOrdering {

  /**
    * eg:
    * {{{
    *   rc4
    * }}}
    */
  final def lowercase: String = SemanticVersionShows.lowercase.noSeparator.show(this)

  /**
    * eg:
    * {{{
    *   rc.4
    * }}}
    */
  final def lowercaseWithDots: String = SemanticVersionShows.lowercase.withDotSeparator.show(this)

  /**
    * eg:
    * {{{
    *   RC4
    * }}}
    */
  final def uppercase: String = SemanticVersionShows.uppercase.noSeparator.show(this)

  /**
    * eg:
    * {{{
    *   RC.4
    * }}}
    */
  final def uppercaseWithDots: String = SemanticVersionShows.uppercase.withDotSeparator.show(this)

  override def toString: String = uppercase
}

object Labels {
  def snapshot: Label = Snapshot

  def alpha: Label = AlphaSingleton

  def alpha(v: Int): Label = Alpha(v)

  def beta: Label = BetaSingleton

  def beta(v: Int): Label = Beta(v)

  def rc(v: Int): Label = ReleaseCandidate(v)

  def m(v: Int): Label = Milestone(v)

  private[semver] case object Snapshot extends Label with SnapshotOrdering

  private[semver] case object AlphaSingleton extends Label with AlphaSingletonOrdering

  private[semver] case class Alpha(alpha: Int) extends Label with AlphaOrdering

  private[semver] case object BetaSingleton extends Label with BetaSingletonOrdering

  private[semver] case class Beta(beta: Int) extends Label with BetaOrdering

  private[semver] case class ReleaseCandidate(rc: Int) extends Label with ReleaseCandidateOrdering

  private[semver] case class Milestone(m: Int) extends Label with MilestoneOrdering

}
