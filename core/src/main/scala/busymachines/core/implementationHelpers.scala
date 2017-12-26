package busymachines.core

import busymachines.core.Anomaly.Parameters

/**
  * Nothing from this file should ever escape [[busymachines.core]]
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
private[core] trait AnomalyConstructors[Resulting <: Anomaly] {
  def apply(id: AnomalyID): Resulting

  def apply(message: String): Resulting

  def apply(parameters: Parameters): Resulting

  def apply(id: AnomalyID, message: String): Resulting

  def apply(id: AnomalyID, parameters: Parameters): Resulting

  def apply(message: String, parameters: Parameters): Resulting

  def apply(id: AnomalyID, message: String, parameters: Parameters): Resulting

  def apply(a: Anomaly): Resulting
}

private[core] trait FailureConstructors[Resulting <: AnomalousFailure] extends AnomalyConstructors[Resulting] {

  def apply(causedBy: Throwable): Resulting

  def apply(id: AnomalyID, message: String, causedBy: Throwable): Resulting

  def apply(id: AnomalyID, parameters: Parameters, causedBy: Throwable): Resulting

  def apply(message: String, parameters: Parameters, causedBy: Throwable): Resulting

  def apply(id: AnomalyID, message: String, parameters: Parameters, causedBy: Throwable): Resulting

  def apply(a: Anomaly, causedBy: Throwable): Resulting
}
