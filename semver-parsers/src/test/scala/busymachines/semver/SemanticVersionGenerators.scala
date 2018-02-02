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

import org.scalacheck.Gen

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
trait SemanticVersionGenerators {

  private val snapshotLabelGen:  Gen[Label] = Gen.const(Labels.snapshot)
  private val alphaSingletonGen: Gen[Label] = Gen.const(Labels.alpha)
  private val alphaGen:          Gen[Label] = Gen.posNum[Int].map(i => Labels.alpha(i))
  private val betaSingletonGen:  Gen[Label] = Gen.const(Labels.beta)
  private val betaGen:           Gen[Label] = Gen.posNum[Int].map(i => Labels.beta(i))
  private val rcGen:             Gen[Label] = Gen.posNum[Int].map(i => Labels.rc(i))
  private val mGen:              Gen[Label] = Gen.posNum[Int].map(i => Labels.m(i))

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
      major    <- Gen.posNum[Int]
      minor    <- Gen.posNum[Int]
      patch    <- Gen.posNum[Int]
      optLabel <- Gen.option(labelGenerator)
      optMeta  <- Gen.option(Gen.alphaStr)
    } yield
      SemanticVersion(
        major = major,
        minor = minor,
        patch = patch,
        label = optLabel,
        meta  = optMeta
      )

  }

}
