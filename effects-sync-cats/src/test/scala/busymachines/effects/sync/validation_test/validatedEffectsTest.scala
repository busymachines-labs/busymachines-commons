package busymachines.effects.sync.validation_test

import org.scalatest._

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.sync.validated._

import cats._, cats.implicits._ //, cats.data._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
private[validation_test] object PWDValidator {
  private type Password = String

  def apply(s: Password): Validated[Unit] = {
    val l: List[Validated[Any]] = List(validateSpaces(s), validateSize(s))
    l.sequence_
  }

  private def validateSpaces(s: Password): Validated[Unit] = {
    s.contains(" ").invalidOnTrue(InvSpaces)
  }

  private def validateSize(s: Password): Validated[Unit] = {
    (s.length < 4).invalidOnTrue(InvSize)
  }

  case object InvSpaces extends InvalidInputFailure("cannot contain spaces")
  case object InvSize extends InvalidInputFailure("must have size of at least 4")
}

class ValidatedEffectsTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  private val valid     = "test"
  private val invSpaces = "te st"
  private val invSize   = "te"
  private val invBoth   = "t s"

  describe("validation") {

    test("it should accept valid password") {
      val v = PWDValidator(valid)
      assert(v == Validated.unit)
    }

    test("reject invSpaces") {
      val v = PWDValidator(invSpaces)
      assert(v == Validated.fail(PWDValidator.InvSpaces))
    }

    test("reject invSize") {
      val v = PWDValidator(invSize)
      assert(v == Validated.fail(PWDValidator.InvSize))
    }

    test("reject both") {
      val v = PWDValidator(invBoth)
      //FIXME: create some scalatest DSL to allow testing this stuff easier
      assert(v == Validated.fail(PWDValidator.InvSpaces, PWDValidator.InvSize))
    }

  }

}
