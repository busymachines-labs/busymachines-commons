package busymachines.json_test.autoderive

import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
class JsonResultAutoDerivationTest extends FlatSpec {

  import busymachines.json_test._
  import busymachines.core._
  import busymachines.effects.result._

  import busymachines.json._
  import busymachines.json.autoderive._
  import busymachines.json.syntax._
  import busymachines.json.ResultJsonCodec._

  it should "... be able to serialize/deserialize Correct(AnarchistMelon)" in {
    val anarchistMelon: Result[AnarchistMelon] =
      Result.pure(AnarchistMelon(noGods = true, noMasters = true, noSuperTypes = true))
    val asJson = anarchistMelon.asJson.spaces2
    val read   = asJson.unsafeDecodeAs[Result[AnarchistMelon]]
    assertResult(anarchistMelon)(read)
  }

  it should "... be able to serialize/deserialize Incorrect(AnarchistMelon) — NotFoundFailure" in {
    val anarchistMelon = Result.fail[AnarchistMelon](NotFoundFailure("anarchist melon not found"))
    val asJson         = anarchistMelon.asJson.spaces2
    val read           = asJson.unsafeDecodeAs[Result[AnarchistMelon]]

    for {
      original <- anarchistMelon.left
      decoded  <- read.left
    } yield {
      assert(original.id === decoded.id,                 "... id")
      assert(original.message === decoded.message,       "... message")
      assert(original.parameters === decoded.parameters, "... parameters")
    }
  }

  it should "... be able to serialize/deserialize Incorrect(AnarchistMelon) — Anomalies" in {
    val anomaly1 = NotFoundFailure("notFound",    Anomaly.Parameters("one"   -> "1"))
    val anomaly2 = UnauthorizedFailure("unauth",  Anomaly.Parameters("two"   -> "2"))
    val anomaly3 = ForbiddenFailure("no_access",  Anomaly.Parameters("three" -> "3"))
    val anomaly4 = DeniedFailure("denied",        Anomaly.Parameters("four"  -> "4"))
    val anomaly5 = InvalidInputFailure("invalid", Anomaly.Parameters("five"  -> "5"))
    val anomaly6 = ConflictFailure("conflict",    Anomaly.Parameters("six"   -> "6"))
    val anomalies = Anomalies(
      AnomalyID("1234"),
      "a lot of failures",
      anomaly1,
      anomaly2,
      anomaly3,
      anomaly4,
      anomaly5,
      anomaly6
    )

    val anarchistMelon = Result.fail[AnarchistMelon](anomalies)
    val asJson         = anarchistMelon.asJson.spaces2
    val read           = asJson.unsafeDecodeAs[Result[AnarchistMelon]]

    for {
      original <- anarchistMelon.left
      decoded  <- read.left
    } yield {
      assert(original.isInstanceOf[Anomalies], "... original expected multiple anomalies")
      val originalAnomalies = original.asInstanceOf[Anomalies]

      assert(decoded.isInstanceOf[Anomalies], "... decoded expected multiple anomalies")
      val decodedAnomalies = original.asInstanceOf[Anomalies]

      assert(originalAnomalies.id === decodedAnomalies.id,                 "... id")
      assert(originalAnomalies.message === decodedAnomalies.message,       "... message")
      assert(originalAnomalies.parameters === decodedAnomalies.parameters, "... parameters")

      assert(originalAnomalies.messages.length === decodedAnomalies.messages.length, "... same amount of anomalies")
    }
  }

}
