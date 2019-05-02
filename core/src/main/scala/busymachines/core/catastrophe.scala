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
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait Catastrophe extends Anomaly with Product with Serializable

private[core] case object CatastrophicErrorID extends AnomalyID with Product with Serializable {
  override val name: String = "CE_0"
}

/**
  * [[java.lang.Error]] is a reasonable choice for a super-class.
  * It's not caught by NonFatal(_)pattern matches. Which means that
  * we can properly propagate "irrecoverable errors" one a per "request"
  * basis and at the same time not crash our application into oblivion.
  */
abstract class CatastrophicError(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends Error(message, causedBy.orNull) with Catastrophe with Product with Serializable {
  override def id: AnomalyID = CatastrophicErrorID

  final override def asThrowable: Throwable = this
}

object CatastrophicError extends CatastrophicErrorConstructors[CatastrophicError] {
  private[core] val CatastrophicErrorMsg = "Catastrophic Error"

  override def apply(causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(message = causedBy.getMessage, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): CatastrophicError =
    CatastrophicErrorImpl(id = id)

  override def apply(message: String): CatastrophicError =
    CatastrophicErrorImpl(message = message)

  override def apply(parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): CatastrophicError =
    CatastrophicErrorImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): CatastrophicError =
    CatastrophicErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(a.asThrowable))

  override def apply(message: String, causedBy: Throwable): CatastrophicError =
    CatastrophicErrorImpl(message = message, causedBy = Option(causedBy))
}

final private[core] case class CatastrophicErrorImpl(
  override val id:         AnomalyID          = CatastrophicErrorID,
  override val message:    String             = CatastrophicError.CatastrophicErrorMsg,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
  causedBy:                Option[Throwable]  = None,
) extends CatastrophicError(message, causedBy = causedBy)

//=============================================================================
//=============================================================================
//=============================================================================

trait InconsistentStateCatastrophe extends Catastrophe

private[core] case object InconsistentStateCatastropheID extends AnomalyID with Product with Serializable {
  override val name: String = "IS_0"
}

abstract class InconsistentStateError(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends CatastrophicError(message, causedBy) with InconsistentStateCatastrophe with Product with Serializable {
  override def id: AnomalyID = InconsistentStateCatastropheID
}

object InconsistentStateError extends CatastrophicErrorConstructors[InconsistentStateError] {
  private[core] val InconsistentStateErrorMsg: String = "Inconsistent State Error"

  override def apply(causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(message = causedBy.getMessage, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(
    id:         AnomalyID,
    message:    String,
    parameters: Parameters,
    causedBy:   Throwable,
  ): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): InconsistentStateError =
    InconsistentStateErrorImpl(id = id)

  override def apply(message: String): InconsistentStateError =
    InconsistentStateErrorImpl(message = message)

  override def apply(parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): InconsistentStateError =
    InconsistentStateErrorImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): InconsistentStateError =
    InconsistentStateErrorImpl(
      id         = a.id,
      message    = a.message,
      parameters = a.parameters,
      causedBy   = Option(a.asThrowable),
    )

  override def apply(message: String, causedBy: Throwable): InconsistentStateError =
    InconsistentStateErrorImpl(message = message, causedBy = Option(causedBy))
}

final private[core] case class InconsistentStateErrorImpl(
  override val id:         AnomalyID          = InconsistentStateCatastropheID,
  override val message:    String             = InconsistentStateError.InconsistentStateErrorMsg,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
  causedBy:                Option[Throwable]  = None,
) extends InconsistentStateError(message, causedBy = causedBy)
