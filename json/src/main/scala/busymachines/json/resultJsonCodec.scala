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
import busymachines.effects.sync._
import io.circe.Decoder.{Result => DecoderResult}
import io.circe.DecodingFailure
import cats.implicits._

import scala.util.control.NonFatal

/**
  *
  * The [[ResultJsonCodecImpl]] intentionally has code duplication to avoid extra allocation
  * of JsonEncoders.
  *
  * You should avoid importing this together with [[AnomalyJsonCodec]] because then
  * automatic derivation picks up
  *
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
object ResultJsonCodec extends ResultJsonCodec

trait ResultJsonCodec {
  implicit final def bmCommonsResultEncoder[T](implicit encode: Encoder[T]): Encoder[Result[T]] =
    new ResultJsonEncoderImpl[T](
      encode,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )

  implicit final def bmCommonsResultDecoder[T](implicit decode: Decoder[T]): Decoder[Result[T]] =
    new ResultJsonDecoderImpl[T](
      decode,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )

  implicit final def bmCommonsResultCodec[T](implicit codec: Codec[T]): Codec[Result[T]] =
    new ResultJsonCodecImpl[T](
      codec,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )
}

private[json] final class ResultJsonEncoderImpl[T](
  private val encode:         Encoder[T],
  private val anomalyCodec:   Codec[Anomaly],
  private val anomaliesCodec: Codec[Anomalies]
) extends Encoder[Result[T]] {

  override def apply(a: Result[T]): Json = a match {
    case Correct(t) => encode(t)
    case Incorrect(a) =>
      a match {
        case a: Anomalies => anomaliesCodec(a)
        case a: Anomaly   => anomalyCodec(a)
      }
  }
}

private[json] final class ResultJsonDecoderImpl[T](
  private val decode:         Decoder[T],
  private val anomalyCodec:   Codec[Anomaly],
  private val anomaliesCodec: Codec[Anomalies]
) extends Decoder[Result[T]] {
  override def apply(c: HCursor): DecoderResult[Result[T]] = {
    val potentialAnomaly: Either[DecodingFailure, Anomaly] =
      anomaliesCodec(c).recoverWith[DecodingFailure, Anomaly] {
        case NonFatal(_) => anomalyCodec(c)
      }
    if (potentialAnomaly.isRight) {
      potentialAnomaly.map(a => Result.fail(a))
    }
    else {
      decode(c).map(Result.pure)
    }
  }
}

private[json] final class ResultJsonCodecImpl[T](
  private val codec:          Codec[T],
  private val anomalyCodec:   Codec[Anomaly],
  private val anomaliesCodec: Codec[Anomalies]
) extends Codec[Result[T]] {
  override def apply(a: Result[T]): Json = a match {
    case Correct(t) => codec(t)
    case Incorrect(a) =>
      a match {
        case a: Anomalies => anomaliesCodec(a)
        case a: Anomaly   => anomalyCodec(a)

      }
  }

  override def apply(c: HCursor): DecoderResult[Result[T]] = {
    val potentialAnomaly: Either[DecodingFailure, Anomaly] =
      anomaliesCodec(c).recoverWith[DecodingFailure, Anomaly] {
        case NonFatal(_) => anomalyCodec(c)
      }
    if (potentialAnomaly.isRight) {
      potentialAnomaly.map(a => Result.fail(a))
    }
    else {
      codec(c).map(Result.pure)
    }
  }
}
