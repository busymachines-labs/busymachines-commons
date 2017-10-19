package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
trait DefaultTypeDiscriminatorConfig {

  private[this] final val JsonTypeString: String = "_type"

  /**
    * This exists to give us the default behavior of deserializing sealed trait
    * hierarchies by adding and "_type" field to the json, instead of creating
    * a property for each variant.
    *
    * Unfortunately, this uses the name of each variant as the value for the
    * "_type" field, leaving JSON-value APIs vulnerable to rename refactorings.
    *
    */
  final implicit val defaultDerivationConfiguration: io.circe.generic.extras.Configuration =
    io.circe.generic.extras.Configuration.default
      .withDiscriminator(JsonTypeString)

}
