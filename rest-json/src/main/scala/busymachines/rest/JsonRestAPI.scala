package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core.{exceptions => dex}
import busymachines.core._
import busymachines.json.{AnomalyJsonCodec, FailureMessageJsonCodec}

/**
  *
  * Simply provides a JSON implementation for everything abstract in [[busymachines.rest.RestAPI]].
  * It is ready to use as is
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPI extends RestAPI with jsonrest.JsonSupport {

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  protected override val failureMessageMarshaller: ToEntityMarshaller[dex.FailureMessage] =
    marshaller(FailureMessageJsonCodec.failureMessageCodec)

  @scala.deprecated("Will be removed in 0.3.0 — Use AnomalyJsonCodec", "0.2.0")
  protected override val failureMessagesMarshaller: ToEntityMarshaller[dex.FailureMessages] =
    marshaller(FailureMessageJsonCodec.failureMessagesCodec)

  override protected val anomalyMarshaller: ToEntityMarshaller[Anomaly] =
    marshaller(AnomalyJsonCodec.AnomalyCodec)

  override protected val anomaliesMarshaller: ToEntityMarshaller[Anomalies] =
    marshaller(AnomalyJsonCodec.AnomaliesCodec)
}
