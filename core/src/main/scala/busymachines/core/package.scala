package busymachines

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
package object core {
  implicit final def anomalyParamValueStringWrapper(s: String): Anomaly.Parameter =
    StringWrapper(s)

  implicit final def anomalyParamValueSeqOfStringWrapper(ses: Seq[String]): Anomaly.Parameter =
    SeqStringWrapper(ses.toVector)
}
