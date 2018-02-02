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
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
trait Anomalies extends Anomaly with Product with Serializable {
  def firstAnomaly: Anomaly

  def restOfAnomalies: immutable.Seq[Anomaly]

  final def messages: immutable.Seq[Anomaly] =
    firstAnomaly +: restOfAnomalies
}

object Anomalies {

  def apply(id: AnomalyID, message: String, msg: Anomaly, msgs: Anomaly*): Anomalies = {
    AnomaliesImpl(id, message, msg, msgs.toList)
  }
}

private[core] final case class AnomaliesImpl(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends Anomalies {

  override def asThrowable: Throwable = AnomalousFailuresImpl(
    id,
    message,
    firstAnomaly,
    restOfAnomalies
  )
}

abstract class AnomalousFailures(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends AnomalousFailure(message) with Anomalies with Product with Serializable

object AnomalousFailures {

  def apply(id: AnomalyID, message: String, msg: Anomaly, msgs: Anomaly*): AnomalousFailures = {
    AnomalousFailuresImpl(id, message, msg, msgs.toList)
  }
}

private[core] case class AnomalousFailuresImpl(
  override val id:              AnomalyID,
  override val message:         String,
  override val firstAnomaly:    Anomaly,
  override val restOfAnomalies: immutable.Seq[Anomaly],
) extends AnomalousFailures(id, message, firstAnomaly, restOfAnomalies) with Product with Serializable
