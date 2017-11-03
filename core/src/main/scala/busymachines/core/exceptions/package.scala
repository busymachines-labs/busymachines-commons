package busymachines.core

import scala.collection.immutable
import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 31 Jul 2017
  *
  */
package object exceptions {

  implicit final def failureMessageParamValueStringWrapper(s: String): FailureMessage.ParamValue =
    FailureMessage.StringWrapper(s)

  implicit final def failureMessageParamValueSeqOfStringWrapper(ses: immutable.Seq[String]): FailureMessage.ParamValue =
    FailureMessage.SeqStringWrapper(ses)
}
