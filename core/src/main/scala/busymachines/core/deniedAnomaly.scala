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
package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait DeniedAnomaly extends Anomaly with MeaningfulAnomalies.Denied with Product with Serializable

object DeniedAnomaly extends AnomalyConstructors[DeniedAnomaly] {
  override def apply(id: AnomalyID): DeniedAnomaly = DeniedAnomalyImpl(id = id)

  override def apply(message: String): DeniedAnomaly = DeniedAnomalyImpl(message = message)

  override def apply(parameters: Parameters): DeniedAnomaly = DeniedAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): DeniedAnomaly =
    DeniedAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): DeniedAnomaly =
    DeniedAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

private[core] final case class DeniedAnomalyImpl(
  override val id:         AnomalyID  = DeniedAnomalyID,
  override val message:    String     = MeaningfulAnomalies.DeniedMsg,
  override val parameters: Parameters = Parameters.empty
) extends DeniedAnomaly with Product with Serializable {

  override def asThrowable: Throwable = DeniedFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class DeniedFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with DeniedAnomaly with Product with Serializable {
  override def id: AnomalyID = DeniedAnomalyID
}

object DeniedFailure
    extends DeniedFailure(MeaningfulAnomalies.DeniedMsg, None) with SingletonAnomalyProduct
    with FailureConstructors[DeniedFailure] {
  override def apply(causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(message = causedBy.getMessage, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): DeniedFailure =
    DeniedFailureImpl(id = id)

  override def apply(message: String): DeniedFailure =
    DeniedFailureImpl(message = message)

  override def apply(parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): DeniedFailure =
    DeniedFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): DeniedFailure =
    DeniedFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): DeniedFailure =
    DeniedFailureImpl(id = a.id, message = a.message, parameters = a.parameters)

  override def apply(message: String, causedBy: Throwable): DeniedFailure =
    DeniedFailureImpl(message = message, causedBy = Option(causedBy))
}

private[core] final case class DeniedFailureImpl(
  override val id:         AnomalyID         = DeniedAnomalyID,
  override val message:    String            = MeaningfulAnomalies.DeniedMsg,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends DeniedFailure(message, causedBy) with Product with Serializable
