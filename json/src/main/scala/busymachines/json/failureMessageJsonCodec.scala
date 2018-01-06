package busymachines.json

import busymachines.core.exceptions._
import io.circe.Decoder.Result
import io.circe.DecodingFailure

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
@scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
object FailureMessageJsonCodec extends FailureMessageJsonCodec

@scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
trait FailureMessageJsonCodec {

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  private implicit final val FailureIDCodec: Codec[FailureID] = new Codec[FailureID] {
    override def apply(c: HCursor): Result[FailureID] = {
      c.as[String].right.map(FailureID.apply)
    }

    override def apply(a: FailureID): Json = Json.fromString(a.name)
  }

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  private implicit final val StringOrSeqCodec: Codec[FailureMessage.ParamValue] = new Codec[FailureMessage.ParamValue] {
    override def apply(a: FailureMessage.ParamValue): Json = {
      a match {
        case FailureMessage.StringWrapper(s)      => Json.fromString(s)
        case FailureMessage.SeqStringWrapper(ses) => Json.fromValues(ses.map(Json.fromString))
      }
    }

    override def apply(c: HCursor): io.circe.Decoder.Result[FailureMessage.ParamValue] = {
      val sa: Result[String] = c.as[String]
      if (sa.isRight) {
        sa.right.map(FailureMessage.ParamValue.apply)
      }
      else {
        c.as[List[String]].right.map(FailureMessage.ParamValue.apply)
      }
    }
  }

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  private implicit final val FailureMessageParamsCodec: Codec[FailureMessage.Parameters] =
    new Codec[FailureMessage.Parameters] {
      override def apply(c: HCursor): Result[FailureMessage.Parameters] = {
        val jsonObj = c.as[JsonObject]
        val m       = jsonObj.right.map(_.toMap)
        val m2: Either[DecodingFailure, Either[DecodingFailure, FailureMessage.Parameters]] = m.right.map {
          (e: Map[String, Json]) =>
            val potentialFailures = e.map { p =>
              p._2.as[FailureMessage.ParamValue].right.map(s => (p._1, s))
            }.toList

            if (potentialFailures.nonEmpty) {
              val first: Either[DecodingFailure, List[(String, FailureMessage.ParamValue)]] =
                potentialFailures.head.right.map(e => List(e))
              val rest = potentialFailures.tail
              val r: Either[DecodingFailure, List[(String, FailureMessage.ParamValue)]] = rest.foldRight(first) {
                (v, acc) =>
                  for {
                    prevAcc <- acc.right
                    newVal  <- v.right
                  } yield prevAcc :+ newVal
              }
              r.right.map(l => FailureMessage.Parameters.apply(l: _*))
            }
            else {
              Right[DecodingFailure, FailureMessage.Parameters](FailureMessage.Parameters.empty)
            }
        }
        m2.right.flatMap(x => identity(x))
      }

      override def apply(a: FailureMessage.Parameters): Json = {
        if (a.isEmpty) {
          Json.fromJsonObject(JsonObject.empty)
        }
        else {
          val parametersJson = a.map { p =>
            (p._1, StringOrSeqCodec(p._2))
          }
          io.circe.Json.fromFields(parametersJson)
        }
      }
    }

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  final implicit val failureMessageCodec: Codec[FailureMessage] = new Codec[FailureMessage] {
    override def apply(c: HCursor): io.circe.Decoder.Result[FailureMessage] = {
      for {
        id     <- c.get[FailureID](CoreJsonConstants.id)
        msg    <- c.get[String](CoreJsonConstants.message)
        params <- c.getOrElse[FailureMessage.Parameters](CoreJsonConstants.parameters)(FailureMessage.Parameters.empty)
      } yield FailureMessage(id, msg, params)
    }

    override def apply(a: FailureMessage): Json = {
      val id      = FailureIDCodec(a.id)
      val message = Json.fromString(a.message)
      if (a.parameters.isEmpty) {
        Json.obj(
          CoreJsonConstants.id      -> id,
          CoreJsonConstants.message -> message
        )
      }
      else {
        val params = FailureMessageParamsCodec(a.parameters)
        Json.obj(
          CoreJsonConstants.id         -> id,
          CoreJsonConstants.message    -> message,
          CoreJsonConstants.parameters -> params
        )
      }
    }
  }

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  final implicit val failureMessagesCodec: Codec[FailureMessages] = new Codec[FailureMessages] {
    override def apply(a: FailureMessages): Json = {
      val fm          = failureMessageCodec.apply(a)
      val arr         = a.messages.map(failureMessageCodec.apply)
      val messagesObj = Json.obj(CoreJsonConstants.messages -> Json.arr(arr: _*))
      messagesObj.deepMerge(fm)
    }

    override def apply(c: HCursor): Result[FailureMessages] = {
      for {
        fm   <- c.as[FailureMessage].right
        msgs <- c.get[Seq[FailureMessage]](CoreJsonConstants.messages).right
        _ <- (if (msgs.isEmpty)
                Left(DecodingFailure("FailureMessages.message needs to be non empty array", c.history))
              else
                Right.apply(())).right
      } yield FailureMessages.apply(fm.id, fm.message, msgs.head, msgs.tail: _*)
    }
  }
}
