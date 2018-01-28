package busymachines.scalatest

import org.scalatest.FunSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
trait FunSpecAlias extends FunSpec {
  def test: ItWord = it
}
