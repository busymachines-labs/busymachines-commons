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
trait UnauthorizedAnomaly extends Anomaly with MeaningfulAnomalies.Unauthorized with Product with Serializable

object UnauthorizedAnomaly extends AnomalyConstructors[UnauthorizedAnomaly] {
  override def apply(id: AnomalyID): UnauthorizedAnomaly = UnauthorizedAnomalyImpl(id = id)

  override def apply(message: String): UnauthorizedAnomaly = UnauthorizedAnomalyImpl(message = message)

  override def apply(parameters: Parameters): UnauthorizedAnomaly = UnauthorizedAnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): UnauthorizedAnomaly =
    UnauthorizedAnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): UnauthorizedAnomaly =
    UnauthorizedAnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): UnauthorizedAnomaly =
    UnauthorizedAnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): UnauthorizedAnomaly =
    UnauthorizedAnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): UnauthorizedAnomaly =
    UnauthorizedAnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)
}

final private[core] case class UnauthorizedAnomalyImpl(
  override val id:         AnomalyID  = UnauthorizedAnomalyID,
  override val message:    String     = MeaningfulAnomalies.UnauthorizedMsg,
  override val parameters: Parameters = Parameters.empty,
) extends UnauthorizedAnomaly with Product with Serializable {

  override def asThrowable: Throwable = UnauthorizedFailureImpl(id, message, parameters)
}

//=============================================================================
//=============================================================================
//=============================================================================

abstract class UnauthorizedFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends AnomalousFailure(message, causedBy) with UnauthorizedAnomaly with Product with Serializable {
  override def id: AnomalyID = UnauthorizedAnomalyID
}

object UnauthorizedFailure
    extends UnauthorizedFailure(MeaningfulAnomalies.UnauthorizedMsg, None) with SingletonAnomalyProduct
    with FailureConstructors[UnauthorizedFailure] {
  override def apply(causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(message = causedBy.getMessage, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id)

  override def apply(message: String): UnauthorizedFailure =
    UnauthorizedFailureImpl(message = message)

  override def apply(parameters: Parameters): UnauthorizedFailure =
    UnauthorizedFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): UnauthorizedFailure =
    UnauthorizedFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): UnauthorizedFailure =
    UnauthorizedFailureImpl(id = a.id, message = a.message, parameters = a.parameters)

  override def apply(message: String, causedBy: Throwable): UnauthorizedFailure =
    UnauthorizedFailureImpl(message = message, causedBy = Option(causedBy))
}

final private[core] case class UnauthorizedFailureImpl(
  override val id:         AnomalyID         = UnauthorizedAnomalyID,
  override val message:    String            = MeaningfulAnomalies.UnauthorizedMsg,
  override val parameters: Parameters        = Parameters.empty,
  causedBy:                Option[Throwable] = None,
) extends UnauthorizedFailure(message, causedBy) with Product with Serializable
