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
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 13 Nov 2017
  *
  */
private[semver] trait LabelOrdering extends Ordered[Label]

/**
  * -SNAPSHOT label is always "less than" any other label,
  * and since they are not numbered, it's "equal" to any other
  * label
  */
private[semver] trait SnapshotOrdering extends Ordered[Label] {
  this: Labels.Snapshot.type =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => EQ
    case AlphaSingleton => LT
    case _: Alpha => LT
    case BetaSingleton => LT
    case _: Beta             => LT
    case _: Milestone        => LT
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait AlphaSingletonOrdering extends Ordered[Label] {
  this: Labels.AlphaSingleton.type =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => GT
    case AlphaSingleton => EQ
    case _: Alpha => LT
    case BetaSingleton => LT
    case _: Beta             => LT
    case _: Milestone        => LT
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait AlphaOrdering extends Ordered[Label] {
  this: Labels.Alpha =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot         => GT
    case AlphaSingleton   => GT
    case Alpha(thatAlpha) => this.alpha.compareTo(thatAlpha)
    case BetaSingleton    => LT
    case _: Beta             => LT
    case _: Milestone        => LT
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait BetaSingletonOrdering extends Ordered[Label] {
  this: Labels.BetaSingleton.type =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => GT
    case AlphaSingleton => GT
    case _: Alpha => GT
    case BetaSingleton => EQ
    case _: Beta             => LT
    case _: Milestone        => LT
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait BetaOrdering extends Ordered[Label] {
  this: Labels.Beta =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => GT
    case AlphaSingleton => GT
    case _: Alpha => GT
    case BetaSingleton  => GT
    case Beta(thatBeta) => this.beta.compareTo(thatBeta)
    case _: Milestone        => LT
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait MilestoneOrdering extends Ordered[Label] {
  this: Labels.Milestone =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => GT
    case AlphaSingleton => GT
    case _: Alpha => GT
    case BetaSingleton => GT
    case _: Beta => GT
    case Milestone(thatM) => this.m.compareTo(thatM)
    case _: ReleaseCandidate => LT
  }
}

private[semver] trait ReleaseCandidateOrdering extends Ordered[Label] {
  this: Labels.ReleaseCandidate =>

  import OrderingUtils._

  override def compare(that: Label): Int = that match {
    case Snapshot       => GT
    case AlphaSingleton => GT
    case _: Alpha => GT
    case BetaSingleton => GT
    case _: Beta      => GT
    case _: Milestone => GT
    case ReleaseCandidate(thatRC) => this.rc.compareTo(thatRC)
  }
}

private[semver] trait SemanticVersionOrdering extends Ordered[SemanticVersion] {
  this: SemanticVersion =>

  /**
    * See ordering rules:
    * In the sem-ver spec:
    * http://semver.org/spec/v2.0.0.html
    *
    * Example:
    * 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0
    */
  override def compare(that: SemanticVersion): Int = {
    import OrderingUtils._
    if (this == that) {
      EQ
    }
    else {
      val majorComp = this.major.compareTo(that.major)
      val minorComp = this.minor.compareTo(that.minor)
      val patchComp = this.patch.compareTo(that.patch)
      val labelComp = (this.label, that.label) match {
        case (None, None)    => EQ
        case (Some(_), None) => LT //basically 1.0.0-RC1 < 1.0.0, regardless of the value of the label
        case (None, Some(_)) => GT //same thing as above
        case (Some(thisLabel), Some(thatLabel)) =>
          thisLabel.compareTo(thatLabel)
      }

      //this should be quite obvious, at each step in the hierarchy, if the numbers are equals
      //then the result is given by the numbers further down in the hierarchy
      if (majorComp == EQ) {
        if (minorComp == EQ) {
          if (patchComp == EQ) {
            if (labelComp == EQ) {
              EQ //technically already handled in that first comparison
            }
            else {
              labelComp
            }
          }
          else {
            patchComp
          }
        }
        else {
          minorComp
        }
      }
      else {
        majorComp
      }
    }
  }
}

private[semver] object OrderingUtils {
  val GT: Int = 1
  val LT: Int = -1
  val EQ: Int = 0
}
