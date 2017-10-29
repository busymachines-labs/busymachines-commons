package busymachines.rest

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
object jsonrest {

  type JsonSupport = akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

  type DefaultJsonProtocol = spray.json.DefaultJsonProtocol
  val DefaultJsonProtocol: spray.json.DefaultJsonProtocol.type = spray.json.DefaultJsonProtocol

}
