package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core._
import busymachines.json.AnomalyJsonCodec

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

  override protected val anomalyMarshaller: ToEntityMarshaller[Anomaly] =
    marshaller(AnomalyJsonCodec.AnomalyCodec)

  override protected val anomaliesMarshaller: ToEntityMarshaller[Anomalies] =
    marshaller(AnomalyJsonCodec.AnomaliesCodec)
}
