package busymachines.json_test

import busymachines.core.exceptions._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
class FailureMessageJsonTest extends FlatSpec {

  import busymachines.json._
  import syntax._
  import FailureMessageJsonCodec._

  behavior of "... serializing simple FailureMessages"

  it should "... encode a NotFoundFailure" in {
    val failure: FailureMessage = NotFoundFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a UnauthorizedFailure" in {
    val failure: FailureMessage = UnauthorizedFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ForbiddenFailure" in {
    val failure: FailureMessage = ForbiddenFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a DeniedFailure" in {
    val failure: FailureMessage = DeniedFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a InvalidInputFailure" in {
    val failure: FailureMessage = InvalidInputFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ConflictFailure" in {
    val failure: FailureMessage = ConflictFailure(
      "test message",
      FailureMessage.Parameters(
        "one" -> "one",
        "two" -> Seq("one", "two")
      )
    )

    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  behavior of "... serializing composite FailureMessages"

  it should "... encode a NotFoundFailures" in {
    val failure: FailureMessages = NotFoundFailures(
      "test message",
      Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "0",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a UnauthorizedFailures" in {
    val failure: FailureMessages = UnauthorizedFailures(
      msg = "test message",
      messages = Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "1",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ForbiddenFailures" in {
    val failure: FailureMessages = ForbiddenFailures(
      "test message",
      Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "2",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a DeniedFailures" in {
    val failure: FailureMessages = DeniedFailures(
      "test message",
      Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "3",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a InvalidInputFailures" in {
    val failure: FailureMessages = InvalidInputFailures(
      "test message",
      Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "4",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }

  it should "... encode a ConflictFailures" in {
    val failure: FailureMessages = ConflictFailures(
      "test message",
      Seq(
        NotFoundFailure(
          "one",
          FailureMessage.Parameters(
            "3" -> "1",
            "4" -> Seq("1", "2")
          )
        ),
        NotFoundFailure(
          "two",
          FailureMessage.Parameters(
            "5" -> "6",
            "6" -> Seq("6", "7")
          )
        )
      )
    )
    val rawJson = failure.asJson.spaces2
    assert(rawJson ==
      """
        |{
        |  "id" : "5",
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
        |""".stripMargin.trim)

    val read = rawJson.unsafeDecodeAs[FailureMessage]
    assert(read.id.name == failure.id.name, "id")
    assert(read.message == failure.message, "message")
    assert(read.parameters == failure.parameters, "parameters")
  }
}
