package busymachines.scalatest

import org.scalatest.FunSpec

/**
  *
  * This is the only suite that allows nesting AND can be decently run in Intellij.
  *
  * But being only able to use the "it" word makes for horrendous English in certain
  * situations. Therefore this creates a simple alias for it — no extra bullshit.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
trait FunSpecAlias extends FunSpec {
  def test: ItWord = it
}
