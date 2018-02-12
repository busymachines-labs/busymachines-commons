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
    JsonRestAPI.anomalyMarshaller

  override protected val anomaliesMarshaller: ToEntityMarshaller[Anomalies] =
    JsonRestAPI.anomaliesMarshaller
}

object JsonRestAPI extends jsonrest.JsonSupport {

  val anomalyMarshaller: ToEntityMarshaller[Anomaly] =
    marshaller(AnomalyJsonCodec.AnomalyCodec)

  val anomaliesMarshaller: ToEntityMarshaller[Anomalies] =
    marshaller(AnomalyJsonCodec.AnomaliesCodec)

  def defaultExceptionHandler: ExceptionHandler = RestAPI.defaultExceptionHandler(
    anomalyMarshaller,
    anomaliesMarshaller
  )

  def defaultExceptionHandlerNoTerminalCase: ExceptionHandler = RestAPI.defaultExceptionHandlerNoTerminalCase(
    anomalyMarshaller,
    anomaliesMarshaller
  )
}
