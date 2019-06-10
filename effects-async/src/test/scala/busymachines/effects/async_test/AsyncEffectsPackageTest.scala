package busymachines.effects.async_test

import busymachines.effects.sync._
import busymachines.effects.async
import org.scalatest.funspec.AnyFunSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Feb 2018
  *
  */
final class AsyncEffectsPackageTest extends AnyFunSpec {
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

    test("validated") {
      async.validated.bmcValidatedAsyncCompanionObjectOps(busymachines.effects.sync.validated.Validated)
    }

    test("future") {
      async.future.bmcFutureBooleanOps(true)
      assert(async.future.ExCtx.toString.size > 0)
    }

  }

}
