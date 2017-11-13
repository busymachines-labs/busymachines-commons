package busymachines.semver

import org.scalacheck.Gen

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
trait SemanticVersionGenerators {

  private val snapshotLabelGen: Gen[Label] = Gen.const(Labels.snapshot)
  private val alphaSingletonGen: Gen[Label] = Gen.const(Labels.alpha)
  private val alphaGen: Gen[Label] = Gen.posNum[Int].map(i => Labels.alpha(i))
  private val betaSingletonGen: Gen[Label] = Gen.const(Labels.beta)
  private val betaGen: Gen[Label] = Gen.posNum[Int].map(i => Labels.beta(i))
  private val rcGen: Gen[Label] = Gen.posNum[Int].map(i => Labels.rc(i))
  private val mGen: Gen[Label] = Gen.posNum[Int].map(i => Labels.m(i))

  implicit val labelGenerator: Gen[Label] = {
    Gen.oneOf(
      snapshotLabelGen,
      alphaSingletonGen,
      alphaGen,
      betaSingletonGen,
      betaGen,
      rcGen,
      mGen
    )
  }

  implicit val semanticVersionGenerator: Gen[SemanticVersion] = {
    for {
      major <- Gen.posNum[Int]
      minor <- Gen.posNum[Int]
      patch <- Gen.posNum[Int]
      optLabel <- Gen.option(labelGenerator)
      optMeta <- Gen.option(Gen.alphaStr)
    } yield SemanticVersion(
      major = major,
      minor = minor,
      patch = patch,
      label = optLabel,
      meta = optMeta
    )

  }


}
