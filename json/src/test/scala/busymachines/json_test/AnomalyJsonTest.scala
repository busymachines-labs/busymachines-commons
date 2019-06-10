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
package busymachines.json_test

import busymachines.core._
import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class AnomalyJsonTest extends AnyFlatSpec {

  import busymachines.json._
  import AnomalyJsonCodec._
  import syntax._

  behavior of "... serializing simple Anomalies"

  it should "... encode a NotFoundFailure" in {
    val failure: Anomaly = NotFoundFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "0",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a UnauthorizedFailure" in {
    val failure: Anomaly = UnauthorizedFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "1",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ForbiddenFailure" in {
    val failure: Anomaly = ForbiddenFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "2",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a DeniedFailure" in {
    val failure: Anomaly = DeniedFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "3",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a InvalidInputFailure" in {
    val failure: Anomaly = InvalidInputFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "4",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ConflictFailure" in {
    val failure: Anomaly = ConflictFailure(
      "test message",
      Anomaly.Parameters(
        "one" -> "one",
        "two" -> List("one", "two"),
      ),
    )

    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """{
          |  "id" : "5",
          |  "message" : "test message",
          |  "parameters" : {
          |    "one" : "one",
          |    "two" : [
          |      "one",
          |      "two"
          |    ]
          |  }
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  behavior of "... serializing composite Anomalies"

  it should "... encode Anomalies" in {
    val failure: Anomalies = Anomalies(
      AnomalyID("test"),
      "test message",
      NotFoundFailure(
        "one",
        Anomaly.Parameters(
          "3" -> "1",
          "4" -> List("1", "2"),
        ),
      ),
      NotFoundFailure(
        "two",
        Anomaly.Parameters(
          "5" -> "6",
          "6" -> List("6", "7"),
        ),
      ),
    )
    val rawJson = failure.asJson.spaces2
    assert(
      rawJson ==
        """
          |{
          |  "id" : "test",
          |  "message" : "test message",
          |  "messages" : [
          |    {
          |      "id" : "0",
          |      "message" : "one",
          |      "parameters" : {
          |        "3" : "1",
          |        "4" : [
          |          "1",
          |          "2"
          |        ]
          |      }
          |    },
          |    {
          |      "id" : "0",
          |      "message" : "two",
          |      "parameters" : {
          |        "5" : "6",
          |        "6" : [
          |          "6",
          |          "7"
          |        ]
          |      }
          |    }
          |  ]
          |}
          |""".stripMargin.trim,
    )

    val read = rawJson.unsafeDecodeAs[Anomaly]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... fail when decoding and empty Anomalies" in {
    val rawJson =
      """
        |{
        |  "id" : "test",
        |  "message" : "test message",
        |  "messages" : []
        |}
        |""".stripMargin.trim

    rawJson.decodeAs[Anomalies] match {
      case Left(_)  => //yey!!!
      case Right(_) => fail("should have failed")
    }
  }
}
