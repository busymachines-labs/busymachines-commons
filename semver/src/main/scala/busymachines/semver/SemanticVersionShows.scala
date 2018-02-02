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

import busymachines.semver.Labels._

/**
  * Since there are multiple string representations of [[SemanticVersion]] in the wild,
  * we provide several out of the box, depending on the two variables:
  *  - lowercase/uppercase labels
  *  - RC.1 vs RC1 — and so on, dot separated labels, or no separation
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
private[semver] object SemanticVersionShows {

  private val EmptyString = ""
  private val Dot         = "."

  object lowercase {
    private val snapshot: String = "snapshot"
    private val alpha:    String = "alpha"
    private val beta:     String = "beta"
    private val m:        String = "m"
    private val rc:       String = "rc"

    object withDotSeparator {
      def show(sv: SemanticVersion): String = genericSVLowercaseShow(sv, Dot)

      def show(l: Label): String = genericLowercaseLabelShow(l, Dot)
    }

    object noSeparator {
      def show(sv: SemanticVersion): String = genericSVLowercaseShow(sv, EmptyString)

      def show(l: Label): String = genericLowercaseLabelShow(l, EmptyString)
    }

    private[lowercase] def genericSVLowercaseShow(sv: SemanticVersion, separator: String): String = {
      val majorMinorPatch = s"${sv.major}.${sv.minor}.${sv.patch}"
      val preReleaseLabel =
        if (sv.label.isEmpty) EmptyString else s"-${genericLowercaseLabelShow(sv.label.get, separator)}"
      val metaLabel = if (sv.meta.isEmpty) EmptyString else s"+${sv.meta.get.trim}"
      s"$majorMinorPatch$preReleaseLabel$metaLabel"
    }

    private[lowercase] def genericLowercaseLabelShow(l: Label, separator: String): String = l match {
      case Snapshot                => snapshot
      case AlphaSingleton          => alpha
      case Alpha(alphaVer)         => s"$alpha$separator$alphaVer"
      case BetaSingleton           => beta
      case Beta(betaVer)           => s"$beta$separator$betaVer"
      case Milestone(mVer)         => s"$m$separator$mVer"
      case ReleaseCandidate(rcVer) => s"$rc$separator$rcVer"
    }
  }

  object uppercase {
    private val SNAPSHOT: String = "SNAPSHOT"
    private val ALPHA:    String = "ALPHA"
    private val BETA:     String = "BETA"
    private val M:        String = "M"
    private val RC:       String = "RC"

    object withDotSeparator {
      def show(sv: SemanticVersion): String = genericSVUppercaseShow(sv, Dot)

      def show(l: Label): String = genericUppercaseLabelShow(l, Dot)
    }

    object noSeparator {
      def show(sv: SemanticVersion): String = genericSVUppercaseShow(sv, EmptyString)

      def show(l: Label): String = genericUppercaseLabelShow(l, EmptyString)
    }

    private[uppercase] def genericSVUppercaseShow(sv: SemanticVersion, separator: String): String = {
      val majorMinorPatch = s"${sv.major}.${sv.minor}.${sv.patch}"
      val preReleaseLabel =
        if (sv.label.isEmpty) EmptyString else s"-${genericUppercaseLabelShow(sv.label.get, separator)}"
      val metaLabel = if (sv.meta.isEmpty) EmptyString else s"+${sv.meta.get.trim}"
      s"$majorMinorPatch$preReleaseLabel$metaLabel"
    }

    private[uppercase] def genericUppercaseLabelShow(l: Label, separator: String): String = l match {
      case Snapshot                => SNAPSHOT
      case AlphaSingleton          => ALPHA
      case Alpha(alphaVer)         => s"$ALPHA$separator$alphaVer"
      case BetaSingleton           => BETA
      case Beta(betaVer)           => s"$BETA$separator$betaVer"
      case Milestone(mVer)         => s"$M$separator$mVer"
      case ReleaseCandidate(rcVer) => s"$RC$separator$rcVer"
    }
  }

}
