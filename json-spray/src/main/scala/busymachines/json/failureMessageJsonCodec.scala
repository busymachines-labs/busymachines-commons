package busymachines.json

import busymachines.core.exceptions.FailureMessage.Parameters
import busymachines.core.exceptions._
import spray.json._

import scala.collection.immutable
import scala.util.control.NonFatal
import scala.util._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
object FailureMessageJsonCodec extends FailureMessageJsonCodec

trait FailureMessageJsonCodec {

  import DefaultJsonProtocol._

  private implicit final val FailureIDCodec: ValueCodec[FailureID] = new ValueCodec[FailureID] {
    override def read(json: Json): FailureID = FailureID(json.convertTo[String])

    override def write(obj: FailureID): Json = JsString(obj.name)
  }

  private implicit final val StringOrSeqCodec: Codec[FailureMessage.ParamValue] = new Codec[FailureMessage.ParamValue] {
    override def write(a: FailureMessage.ParamValue): Json = {
      a match {
        case FailureMessage.StringWrapper(s) => JsString(s)
        case FailureMessage.SeqStringWrapper(ses) =>
          JsArray(ses.map(s => implicitly[JsonFormat[String]].write(s)).toVector)
      }
    }

    override def read(c: Json): FailureMessage.ParamValue = {
      Try(c.convertTo[immutable.Seq[String]])
        .map((s: immutable.Seq[String]) => FailureMessage.ParamValue(s))
        .recoverWith {
          case NonFatal(_) => Try(c.convertTo[String]).map(FailureMessage.ParamValue.apply)
        }
        .get
    }
  }

  final implicit val failureMessageCodec: Codec[FailureMessage] = new Codec[FailureMessage] {
    private val jsonCodec = jsonFormat3(FailureMessageRepr)

    override def read(c: Json): FailureMessage = {
      val rpr = jsonCodec.read(c)
      FailureMessage(rpr.id, rpr.message, rpr.parameters.getOrElse(Parameters.empty))
    }

    override def write(a: FailureMessage): Json = {
      JsonObject(
        CoreJsonConstants.id         -> a.id.toJson,
        CoreJsonConstants.message    -> a.message.toJson,
        CoreJsonConstants.parameters -> a.parameters.toJson
      )
    }
  }

  final implicit val failureMessagesCodec: Codec[FailureMessages] = new Codec[FailureMessages] {
    private val jsonCodec: Codec[FailureMessagesRepr] = jsonFormat3(FailureMessagesRepr)

    override def write(a: FailureMessages): Json = {
      JsonObject(
        CoreJsonConstants.id       -> a.id.toJson,
        CoreJsonConstants.message  -> a.message.toJson,
        CoreJsonConstants.messages -> a.messages.toJson
      )
    }

    override def read(c: Json): FailureMessages = {
      val repr = jsonCodec.read(c)
      FailureMessages(
        id      = repr.id,
        message = repr.message,
        msg     = repr.messages.headOption.getOrElse(deserializationError("Needs to have at least on messages")),
        repr.messages.tail: _*
      )
    }
  }
}



private[json] case class FailureMessageRepr(
  id:         FailureID,
  message:    String,
  parameters: Option[Parameters]
)

private[json] case class FailureMessagesRepr(
  id:       FailureID,
  message:  String,
  messages: immutable.Seq[FailureMessage]
)
