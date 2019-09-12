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
package busymachines.json

import busymachines.core._
import io.circe.Decoder.{Result => DecoderResult}
import io.circe.DecodingFailure

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
object AnomalyJsonCodec extends AnomalyJsonCodec

trait AnomalyJsonCodec {

  implicit final private val AnomalyIDCodec: Codec[AnomalyID] = new Codec[AnomalyID] {
    override def apply(c: HCursor): DecoderResult[AnomalyID] = {
      c.as[String].map(AnomalyID.apply)
    }

    override def apply(a: AnomalyID): Json = Json.fromString(a.name)
  }

  implicit final private val StringOrSeqCodec: Codec[Anomaly.Parameter] = new Codec[Anomaly.Parameter] {
    override def apply(a: Anomaly.Parameter): Json = {
      a match {
        case StringWrapper(s)      => Json.fromString(s)
        case SeqStringWrapper(ses) => Json.fromValues(ses.map(Json.fromString))
      }
    }

    override def apply(c: HCursor): DecoderResult[Anomaly.Parameter] = {
      val sa: DecoderResult[String] = c.as[String]
      if (sa.isRight) {
        sa.map(Anomaly.Parameter)
      }
      else {
        c.as[List[String]].map(Anomaly.Parameter)
      }
    }
  }

  implicit final private val AnomalyParamsCodec: Codec[Anomaly.Parameters] =
    new Codec[Anomaly.Parameters] {
      override def apply(c: HCursor): DecoderResult[Anomaly.Parameters] = {
        val jsonObj = c.as[JsonObject]
        val m       = jsonObj.map(_.toMap)
        val m2: Either[DecodingFailure, Either[DecodingFailure, Anomaly.Parameters]] = m.map { (e: Map[String, Json]) =>
          val potentialFailures = e.map { p =>
            p._2.as[Anomaly.Parameter].map(s => (p._1, s))
          }.toList

          if (potentialFailures.nonEmpty) {
            val first: Either[DecodingFailure, List[(String, Anomaly.Parameter)]] =
              potentialFailures.head.map(e => List(e))
            val rest = potentialFailures.tail
            val r: Either[DecodingFailure, List[(String, Anomaly.Parameter)]] = rest.foldRight(first) { (v, acc) =>
              for {
                prevAcc <- acc
                newVal  <- v
              } yield prevAcc :+ newVal
            }
            r.map(l => Anomaly.Parameters(l: _*))
          }
          else {
            Right[DecodingFailure, Anomaly.Parameters](Anomaly.Parameters.empty)
          }
        }
        m2.flatMap(x => identity(x))
      }

      override def apply(a: Anomaly.Parameters): Json = {
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

  implicit final val AnomalyCodec: Codec[Anomaly] = new Codec[Anomaly] {
    override def apply(c: HCursor): DecoderResult[Anomaly] = {
      for {
        id     <- c.get[AnomalyID](CoreJsonConstants.id)
        msg    <- c.get[String](CoreJsonConstants.message)
        params <- c.getOrElse[Anomaly.Parameters](CoreJsonConstants.parameters)(Anomaly.Parameters.empty)
      } yield Anomaly(id, msg, params)
    }

    override def apply(a: Anomaly): Json = {
      val id      = AnomalyIDCodec(a.id)
      val message = Json.fromString(a.message)
      if (a.parameters.isEmpty) {
        Json.obj(
          CoreJsonConstants.id      -> id,
          CoreJsonConstants.message -> message,
        )
      }
      else {
        val params = AnomalyParamsCodec(a.parameters)
        Json.obj(
          CoreJsonConstants.id         -> id,
          CoreJsonConstants.message    -> message,
          CoreJsonConstants.parameters -> params,
        )
      }
    }
  }

  implicit final val AnomaliesCodec: Codec[Anomalies] = new Codec[Anomalies] {
    override def apply(a: Anomalies): Json = {
      val fm          = AnomalyCodec.apply(a)
      val arr         = a.messages.map(AnomalyCodec.apply)
      val messagesObj = Json.obj(CoreJsonConstants.messages -> Json.arr(arr: _*))
      messagesObj.deepMerge(fm)
    }

    override def apply(c: HCursor): DecoderResult[Anomalies] = {
      for {
        fm   <- c.as[Anomaly]
        msgs <- c.get[Seq[Anomaly]](CoreJsonConstants.messages)
        _ <- (if (msgs.isEmpty)
                Left(DecodingFailure("Anomalies.message needs to be non empty array", c.history))
              else
                Right.apply(()))
      } yield Anomalies.apply(fm.id, fm.message, msgs.head, msgs.tail: _*)
    }
  }
}
