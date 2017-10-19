package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core.exceptions.{FailureMessage, FailureMessages}

/**
  *
  * Simply provides a JSON implementation for everything abstract in [[RestAPI]].
  * It is ready to use as is
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPI extends RestAPI with jsonrest.JsonSupport {

  import JsonRestAPI._

  protected override val failureMessageMarshaller: ToEntityMarshaller[FailureMessage] =
    failure.failureMessageMarshaller

  protected override val failureMessagesMarshaller: ToEntityMarshaller[FailureMessages] = {
    failures.failureMessagesMarshaller
  }

}

private[rest] object JsonRestAPI {


  private object failure {

    import busymachines.json.FailureMessageJsonCodec._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    implicit val failureMessageMarshaller: ToEntityMarshaller[FailureMessage] =
      marshaller[FailureMessage]

  }

  private object failures {

    import busymachines.json.FailureMessageJsonCodec._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    implicit val failureMessagesMarshaller: ToEntityMarshaller[FailureMessages] =
      marshaller[FailureMessages]
  }

}