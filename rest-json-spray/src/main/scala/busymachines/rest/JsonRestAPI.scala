package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core._
import busymachines.core.{exceptions => dex}
import busymachines.json.{AnomalyJsonCodec, FailureMessageJsonCodec}
import busymachines.rest.jsonrest.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * Simply provides a JSON implementation for everything abstract in [[busymachines.rest.RestAPI]].
  * It is ready to use as is
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPI extends RestAPI with JsonSupport {

  implicit class SprayFutureOps[T](val f: Future[T]) {

    def asEmptyResponse(implicit ec: ExecutionContext): Future[HttpEntity.Strict] =
      f map JsonRestAPI.emptyResponseFun
  }

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0-RC8")
  protected override val failureMessageMarshaller: ToEntityMarshaller[dex.FailureMessage] =
    sprayJsonMarshaller(FailureMessageJsonCodec.failureMessageCodec)

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0-RC8")
  protected override val failureMessagesMarshaller: ToEntityMarshaller[dex.FailureMessages] =
    sprayJsonMarshaller(FailureMessageJsonCodec.failureMessagesCodec)

  override protected val anomalyMarshaller: ToEntityMarshaller[Anomaly] =
    sprayJsonMarshaller(AnomalyJsonCodec.AnomalyCodec)

  override protected val anomaliesMarshaller: ToEntityMarshaller[Anomalies] =
    sprayJsonMarshaller(AnomalyJsonCodec.AnomaliesCodec)

}

private[rest] object JsonRestAPI extends JsonSupport {

  private val emptyResponseFun: Any => HttpEntity.Strict = { _ =>
    HttpEntity.Empty
  }
}
