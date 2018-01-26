package busymachines.json

import busymachines.core._
import busymachines.result._
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
  final implicit def bmCommonsResultEncoder[T](implicit encode: Encoder[T]): Encoder[Result[T]] =
    new ResultJsonEncoderImpl[T](
      encode,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )

  final implicit def bmCommonsResultDecoder[T](implicit decode: Decoder[T]): Decoder[Result[T]] =
    new ResultJsonDecoderImpl[T](
      decode,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )

  final implicit def bmCommonsResultCodec[T](implicit codec: Codec[T]): Codec[Result[T]] =
    new ResultJsonCodecImpl[T](
      codec,
      AnomalyJsonCodec.AnomalyCodec,
      AnomalyJsonCodec.AnomaliesCodec
    )

  final def explicitResultCodecHack[T](implicit encode: Encoder[T], decode: Decoder[T]): Codec[Result[T]] = ???
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
