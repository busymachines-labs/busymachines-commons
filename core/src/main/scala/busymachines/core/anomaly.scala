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

import scala.collection.immutable

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait Anomaly extends Product with Serializable {
  def id: AnomalyID

  def message: String

  def parameters: Anomaly.Parameters = Anomaly.Parameters.empty

  /**
    * * In case that "this" is already a Throwable, e.g. it was used in the
    * exception throwing manner to begin with then this is easy.
    *
    * Otherwise, if it was used in the monadic propagation on the lhs of an Either
    * then this is a thorny issue. This method is required for interop with stuff like
    * Future, Try, Task, etc., stuff that encapsulates a Throwable.
    *
    * They lose specific type information about the exception anyway,
    * so there is no point in preserving it in the return type of this method.
    *
    * Therefore, this method should guarantee that the runtime properties of the new
    * Throwable match those of "this". i.e.
    *   - it should have the same [[busymachines.core.MeaningfulAnomalies]] marker at runtime, if any
    *   - and ensure that all properties id, message, parameters, get propagated properly
    *
    */
  def asThrowable: Throwable
}

/**
  * Some suggested naming conventions are put here so that they're easily accessible.
  * These can also be found in the scaladoc of [[busymachines.core.MeaningfulAnomalies]]
  *
  * - [[busymachines.core.MeaningfulAnomalies.NotFound]]
  *   - range: 000-099; e.g. pone_001, ptwo_076, pthree_099
  *
  * - [[busymachines.core.MeaningfulAnomalies.UnauthorizedMsg]]
  *   - range: 100-199; e.g. pone_100, ptwo_176, pthree_199
  *
  * - [[busymachines.core.MeaningfulAnomalies.ForbiddenMsg]]
  *   - range: 200-299; e.g. pone_200, ptwo_276, pthree_299
  *
  * - [[busymachines.core.MeaningfulAnomalies.DeniedMsg]]
  *   - range: 300-399; e.g. pone_300, ptwo_376, pthree_399
  *
  * - [[busymachines.core.MeaningfulAnomalies.InvalidInput]]
  *   - range: 400-499; e.g. pone_400, ptwo_476, pthree_499
  */
trait AnomalyID extends Product with Serializable with Equals {
  def name: String

  final override def canEqual(that: Any): Boolean = that.isInstanceOf[AnomalyID]

  final override def equals(obj: Any): Boolean = canEqual(obj) && this.hashCode() == obj.hashCode()

  final override def hashCode(): Int = name.hashCode * 13

  override def toString: String = name
}

object AnomalyID {
  def apply(id: String): AnomalyID = AnomalyIDUnderlying(id)
}

private[core] case object DefaultAnomalyID extends AnomalyID {
  override val name: String = "DA_0"
}
final private[core] case class AnomalyIDUnderlying(override val name: String) extends AnomalyID

object Anomaly extends AnomalyConstructors[Anomaly] {
  private[core] val Anomaly: String = "Anomaly"

  type Parameter = StringOrSeqString
  def Parameter(s:   String):                StringOrSeqString = StringWrapper(s)
  def Parameter(ses: immutable.Seq[String]): StringOrSeqString = SeqStringWrapper(ses)

  type Parameters = Map[String, Parameter]

  /**
    * the reason why this type signature does not return Parameters is a pragmatic one.
    * Intellij does not infer it correctly in the IDE and yields a false negative.
    *
    * As far as the client code is concerned this is the same, and scalac properly
    * compiles both versions, so we'll keep the one which causes the least misery.
    *
    * Once intellij fixes this (need to look for issue) we can have cleaner code here
    */
  def Parameters(ps: (String, Parameter)*): Map[String, StringOrSeqString] = Map.apply(ps: _*)

  object Parameters {
    def empty: Map[String, StringOrSeqString] = Map.empty[String, Parameter]
  }

  override def apply(id: AnomalyID): Anomaly = AnomalyImpl(id = id)

  override def apply(message: String): Anomaly = AnomalyImpl(message = message)

  override def apply(parameters: Parameters): Anomaly = AnomalyImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): Anomaly =
    AnomalyImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): Anomaly =
    AnomalyImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): Anomaly =
    AnomalyImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): Anomaly =
    AnomalyImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): Anomaly =
    AnomalyImpl(id = a.id, message = a.message, parameters = a.parameters)

}

final private[core] case class AnomalyImpl(
  override val id:         AnomalyID          = DefaultAnomalyID,
  override val message:    String             = Anomaly.Anomaly,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
) extends Anomaly {

  override def asThrowable: Throwable =
    AnomalousFailureImpl(id, message, parameters)
}

/**
  * This is a hack until dotty (scala 3.0) comes along with union types.
  * Until then, boiler plate freedom is given by the implicit
  * conversions found in the package object
  */
sealed trait StringOrSeqString extends Product with Serializable

final case class StringWrapper private (s: String) extends StringOrSeqString

final case class SeqStringWrapper private (ses: immutable.Seq[String]) extends StringOrSeqString

/**
  * The moment an [[busymachines.core.Anomaly]] inherits
  * from the impure works of [[java.lang.Exception]] it is
  * referred to as a "Failure"
  */
abstract class AnomalousFailure(
  override val message: String,
  causedBy:             Option[Throwable] = None,
) extends Exception(message, causedBy.orNull) with Anomaly {
  final override def asThrowable: Throwable = this
}

private[core] case object AnomalousFailureID extends AnomalyID {
  override val name: String = "AF_0"
}

object AnomalousFailure extends FailureConstructors[AnomalousFailure] {
  private[core] val AnomalousFailure = "Anomalous Failure"
  import busymachines.core.Anomaly.Parameters

  override def apply(causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(message = causedBy.getMessage, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(id = id, message = message, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(id = id, parameters = parameters, causedBy = Option(causedBy))

  override def apply(message: String, parameters: Parameters, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(id = id, message = message, parameters = parameters, causedBy = Option(causedBy))

  override def apply(a: Anomaly, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(id = a.id, message = a.message, parameters = a.parameters, causedBy = Option(causedBy))

  override def apply(id: AnomalyID): AnomalousFailure =
    AnomalousFailureImpl(id = id)

  override def apply(message: String): AnomalousFailure =
    AnomalousFailureImpl(message = message)

  override def apply(parameters: Parameters): AnomalousFailure =
    AnomalousFailureImpl(parameters = parameters)

  override def apply(id: AnomalyID, message: String): AnomalousFailure =
    AnomalousFailureImpl(id = id, message = message)

  override def apply(id: AnomalyID, parameters: Parameters): AnomalousFailure =
    AnomalousFailureImpl(id = id, parameters = parameters)

  override def apply(message: String, parameters: Parameters): AnomalousFailure =
    AnomalousFailureImpl(message = message, parameters = parameters)

  override def apply(id: AnomalyID, message: String, parameters: Parameters): AnomalousFailure =
    AnomalousFailureImpl(id = id, message = message, parameters = parameters)

  override def apply(a: Anomaly): AnomalousFailure =
    AnomalousFailureImpl(id = a.id, message = a.message, parameters = a.parameters)

  override def apply(message: String, causedBy: Throwable): AnomalousFailure =
    AnomalousFailureImpl(message = message, causedBy = Option(causedBy))
}

final private[core] case class AnomalousFailureImpl(
  override val id:         AnomalyID          = AnomalousFailureID,
  override val message:    String             = AnomalousFailure.AnomalousFailure,
  override val parameters: Anomaly.Parameters = Anomaly.Parameters.empty,
  causedBy:                Option[Throwable]  = None,
) extends AnomalousFailure(message, causedBy)
