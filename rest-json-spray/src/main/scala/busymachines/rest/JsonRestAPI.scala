package busymachines.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import busymachines.core.exceptions.{FailureMessage, FailureMessages}
import busymachines.json.FailureMessageJsonCodec
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

  import JsonRestAPI._

  implicit class SprayFutureOps[T](val f: Future[T]) {
    def asEmptyResponse(implicit ec: ExecutionContext): Future[HttpEntity.Strict] = f map emptyResponseFun
  }

  protected override val failureMessageMarshaller: ToEntityMarshaller[FailureMessage] =
    failure.failureMessageMarshaller

  protected override val failureMessagesMarshaller: ToEntityMarshaller[FailureMessages] = {
    failures.failureMessagesMarshaller
  }

}

private[rest] object JsonRestAPI extends JsonSupport {

  private val emptyResponseFun: Any => HttpEntity.Strict = { _ =>
    HttpEntity.Empty
  }

  private object failure {

    implicit val failureMessageMarshaller: ToEntityMarshaller[FailureMessage] =
      JsonSupport.sprayJsonMarshaller(FailureMessageJsonCodec.failureMessageCodec)

  }

  private object failures {

    implicit val failureMessagesMarshaller: ToEntityMarshaller[FailureMessages] =
      JsonSupport.sprayJsonMarshaller(FailureMessageJsonCodec.failureMessagesCodec)
  }

}
