package busymachines.effects.sync_test

import busymachines.effects.sync
import busymachines.core.InvalidInputFailure
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
class SyncEffectsPackageTest extends FunSpec {
  private def test: ItWord = it

  test("option") {
    import sync.option._
    assert(Option.pure(1) == Option.pure(1))
  }

  test("try") {
    import sync.tr._
    assert(Try.pure(1) == Try.pure(1))
  }

  test("either") {
    sync.either.bmcEitherEffectReferenceOps(Left(1))

  }

  test("result") {
    import sync.result._

    assert(Result.pure(1) == Result.pure(1))
    assert(Correct(1) == Correct(1))
    assert(Incorrect(InvalidInputFailure) == Incorrect(InvalidInputFailure))
  }

}
