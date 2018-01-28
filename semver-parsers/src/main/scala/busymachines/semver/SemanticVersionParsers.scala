package busymachines.semver

import busymachines.effects.result._

/**
  *
  * A fairly straight-forward definition of parsers.
  *
  * It supports parsing all values outputed by the pretty printing helper methods:
  * [[SemanticVersion.uppercase]], [[SemanticVersion.uppercaseWithDots]],
  * [[SemanticVersion.lowercase]], [[SemanticVersion.lowercaseWithDots]]
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
object SemanticVersionParsers {

  import atto._
  import atto.parser.all._
  import atto.syntax.all._

  def parseSemanticVersion(semver: String): Result[SemanticVersion] = {
    Parser
      .parseOnly(semanticVersionEOI, semver)
      .either
      .left
      .map(parseError => InvalidSemanticVersionFailure(semver, parseError))
  }

  def unsafeParseSemanticVersion(semVer: String): SemanticVersion = {
    parseSemanticVersion(semVer).unsafeGet
  }

  def parseLabel(label: String): Result[Label] = {
    Parser
      .parseOnly(labelParserEOI, label)
      .either
      .left
      .map(parseError => InvalidSemanticVersionLabelFailure(label, parseError))
  }

  def unsafeParseLabel(label: String): Label = {
    this.parseLabel(label).unsafeGet
  }

  private val dotParser:  Parser[Char] = char('.')
  private val dashParser: Parser[Char] = char('-')
  private val plusParser: Parser[Char] = char('+')

  private val SNAPSHOT: String = "SNAPSHOT"
  private val ALPHA:    String = "ALPHA"
  private val BETA:     String = "BETA"
  private val M:        String = "M"
  private val RC:       String = "RC"

  private val snapshotParser: Parser[Label] = stringCI(SNAPSHOT).map(_ => Labels.snapshot)

  private val alphaParser: Parser[Label] = {
    val singleton: Parser[Label] = for {
      _ <- stringCI(ALPHA)
    } yield Labels.alpha

    val completeWithDot: Parser[Label] = for {
      _ <- singleton
      _ <- dotParser
      a <- int
    } yield Labels.alpha(a)

    val completeNoDot: Parser[Label] = for {
      _ <- singleton
      a <- int
    } yield Labels.alpha(a)

    completeWithDot | completeNoDot | singleton
  }

  private val betaParser: Parser[Label] = {
    val singleton: Parser[Label] = for {
      _ <- stringCI(BETA)
    } yield Labels.beta

    val completeWithDot: Parser[Label] = for {
      _ <- singleton
      _ <- dotParser
      b <- int
    } yield Labels.beta(b)

    val completeNoDot: Parser[Label] = for {
      _ <- singleton
      b <- int
    } yield Labels.beta(b)

    completeWithDot | completeNoDot | singleton
  }

  private val milestoneParser: Parser[Label] = {
    val singleton: Parser[String] = stringCI(M)

    val completeWithDot: Parser[Label] = for {
      _ <- singleton
      _ <- dotParser
      m <- int
    } yield Labels.m(m)

    val completeNoDot: Parser[Label] = for {
      _ <- singleton
      m <- int
    } yield Labels.m(m)

    completeWithDot | completeNoDot
  }

  private val releaseCandidateParser: Parser[Label] = {
    val singleton: Parser[String] = stringCI(RC)

    val completeWithDot: Parser[Label] = for {
      _  <- singleton
      _  <- dotParser
      rc <- int
    } yield Labels.rc(rc)

    val completeNoDot: Parser[Label] = for {
      _  <- singleton
      rc <- int
    } yield Labels.rc(rc)

    completeWithDot | completeNoDot
  }

  private val labelParser: Parser[Label] = {
    snapshotParser | alphaParser | betaParser | milestoneParser | releaseCandidateParser
  }

  private val labelParserEOI: Parser[Label] =
    labelParser <~ endOfInput

  private val metaParser: Parser[String] = takeText

  private val semanticVersionParser: Parser[SemanticVersion] = {
    val dashWithLabel = for {
      _     <- dashParser
      label <- labelParser
    } yield label

    val plusWithMeta = for {
      _    <- plusParser
      meta <- metaParser
    } yield meta

    for {
      major    <- int
      _        <- dotParser
      minor    <- int
      _        <- dotParser
      patch    <- int
      optLabel <- opt(dashWithLabel)
      optMeta  <- opt(plusWithMeta)
    } yield
      SemanticVersion(
        major = major,
        minor = minor,
        patch = patch,
        label = optLabel,
        meta  = optMeta
      )
  }

  private val semanticVersionEOI: Parser[SemanticVersion] =
    semanticVersionParser <~ endOfInput

}
