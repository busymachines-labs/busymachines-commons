package busymachines

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
package object core {
  type Parameter         = Anomaly.Param
  type AnomalyParameters = Anomaly.Parameters

  val AnomalyParameters: Anomaly.Parameters.type = Anomaly.Parameters

  implicit final def anomalyParamValueStringWrapper(s: String): Parameter =
    StringWrapper(s)

  implicit final def anomalyParamValueSeqOfStringWrapper(ses: Seq[String]): Parameter =
    SeqStringWrapper(ses.toVector)
}
