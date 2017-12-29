package busymachines.json

import busymachines.core._
import spray.json._

import scala.collection.immutable
import scala.util.control.NonFatal
import scala.util._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Dec 2017
  *
  */
object AnomalyJsonCodec extends AnomalyJsonCodec

trait AnomalyJsonCodec {

  import DefaultJsonProtocol._

  private implicit final val AnomalyIDCodec: ValueCodec[AnomalyID] = new ValueCodec[AnomalyID] {
    override def read(json: Json): AnomalyID = AnomalyID(json.convertTo[String])

    override def write(obj: AnomalyID): Json = JsString(obj.name)
  }

  private implicit final val StringOrSeqCodec: Codec[Anomaly.Parameter] = new Codec[Anomaly.Parameter] {
    override def write(a: Anomaly.Parameter): Json = {
      a match {
        case StringWrapper(s) => JsString(s)
        case SeqStringWrapper(ses) =>
          JsArray(ses.map(s => implicitly[JsonFormat[String]].write(s)).toVector)
      }
    }

    override def read(c: Json): Anomaly.Parameter = {
      Try(c.convertTo[immutable.Seq[String]])
        .map((s: immutable.Seq[String]) => Anomaly.Parameter(s))
        .recoverWith {
          case NonFatal(_) => Try(c.convertTo[String]).map(Anomaly.Parameter)
        }
        .get
    }
  }

  final implicit val AnomalyCodec: Codec[Anomaly] = new Codec[Anomaly] {
    private val jsonCodec = jsonFormat3(AnomalyRepr)

    override def read(c: Json): Anomaly = {
      val rpr = jsonCodec.read(c)
      Anomaly(rpr.id, rpr.message, rpr.parameters.getOrElse(Anomaly.Parameters.empty))
    }

    override def write(a: Anomaly): Json = {
      JsonObject(
        CoreJsonConstants.id         -> a.id.toJson,
        CoreJsonConstants.message    -> a.message.toJson,
        CoreJsonConstants.parameters -> a.parameters.toJson
      )
    }
  }

  final implicit val AnomaliesCodec: Codec[Anomalies] = new Codec[Anomalies] {
    private val jsonCodec: Codec[AnomaliesRepr] = jsonFormat3(AnomaliesRepr)

    override def write(a: Anomalies): Json = {
      JsonObject(
        CoreJsonConstants.id       -> a.id.toJson,
        CoreJsonConstants.message  -> a.message.toJson,
        CoreJsonConstants.messages -> a.messages.toJson
      )
    }

    override def read(c: Json): Anomalies = {
      val repr = jsonCodec.read(c)
      Anomalies(
        id      = repr.id,
        message = repr.message,
        msg     = repr.messages.headOption.getOrElse(deserializationError("Needs to have at least on messages")),
        repr.messages.tail: _*
      )
    }
  }
}

private[json] case class AnomalyRepr(
  id:         AnomalyID,
  message:    String,
  parameters: Option[Anomaly.Parameters]
)

private[json] case class AnomaliesRepr(
  id:       AnomalyID,
  message:  String,
  messages: immutable.Seq[Anomaly]
)
