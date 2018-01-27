package busymachines.effects_test

import busymachines.effects._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 27 Jan 2018
  *
  */
class IOEffectsTest extends FlatSpec with Matchers {
  private implicit val sc: Scheduler = Scheduler.global

  behavior of "IO — from X methods"

  it should "convert from Option" in {}
}
