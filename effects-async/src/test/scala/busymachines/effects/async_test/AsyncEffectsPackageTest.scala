package busymachines.effects.async_test

import busymachines.effects.sync._
import busymachines.effects.async
import org.scalatest.FunSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
final class AsyncEffectsPackageTest extends FunSpec {
  private def test: ItWord = it

  describe("effects") {

    test("option") {
      async.option.bmcOptionAsyncCompanionObjectOps(Option)
    }

    test("try") {
      async.tr.bmcTryAsyncCompanionObjectOps(Try)
    }

    test("either") {
      async.either.bmcEitherAsyncCompanionObjectOps(Either)

    }

    test("result") {
      async.result.bmcResultAsyncCompanionObjectOps(Result)
    }

    test("io") {
      async.io.bmcIOBooleanOps(true)
    }

    test("task") {
      async.task.bmcTaskBooleanOps(true)
    }

    test("future") {
      async.future.bmcFutureBooleanOps(true)
    }

  }

}
