package busymachines.json

import spray.json.{JsString, deserializationError}

import scala.reflect.runtime.universe._


/**
  *
  * This trait excludes the [[spray.json.ProductFormats]] on purpose,
  * because there should be only one way to derive a formatter is through
  * the ``derive`` object
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Oct 2017
  *
  */
trait BusymachinesDefaultJsonCodec extends
  spray.json.BasicFormats with
  spray.json.AdditionalFormats with
  spray.json.StandardFormats with
  spray.json.CollectionFormats {

  implicit class ADTVariantOps[VariantType: TypeTag](c: Codec[VariantType]) {
    def embedField[ADTType >: VariantType]
    (name: String, value: String): BusymachinesDefaultJsonCodec.ADTVariantCodec[ADTType] = {
      new BusymachinesDefaultJsonCodec.ADTVariantCodec[ADTType] {
        private[json] override type ConcreteType = VariantType
        private[json] override val codec: Codec[ConcreteType] = c
        private[json] override val fieldName: String = name
        private[json] override val fieldValue: String = value

        private[json] override val tt: TypeTag[VariantType] = implicitly[TypeTag[VariantType]]
      }
    }

    def embedTypeField[HierarchyType >: VariantType]
    (typeValue: String): BusymachinesDefaultJsonCodec.ADTVariantCodec[HierarchyType] = {
      embedField(BusymachinesDefaultJsonCodec.TypeFieldDiscriminator, typeValue)
    }
  }

  val derive: BusymachinesDefaultJsonCodec.derive.type =
    BusymachinesDefaultJsonCodec.derive
}

private[json] object BusymachinesDefaultJsonCodec {

  /**
    * Basically, it makes a complete un-type-safe assumption that you
    * will pass it a case object, no static guarantees whatsoever towards
    * that end.
    */
  private class EnumValueCodec[T](obj: T) extends ValueCodec[T] {
    val reprString: String = obj.getClass.getSimpleName
    val capturedRef: T = obj
    val jsString: Json = JsString(reprString)

    override def read(json: Json): T = {
      val str = spray.json.DefaultJsonProtocol.StringJsonFormat.read(json)
      if (str.trim != reprString) {
        deserializationError(s"expected '$reprString', got: '$str' enum value")
      } else {
        capturedRef
      }
    }

    override def write(obj: T): Json = jsString
  }

  /**
    * You should access the ``jsonFormatX`` values from this object,
    * and not import them via DefaultJsonProtocol crap
    */
  object derive extends
    DeriveImpl with
    spray.json.AdditionalFormats with
    spray.json.StandardFormats with
    spray.json.ProductFormats

  val TypeFieldDiscriminator: String = "_type"

  private[json] trait ADTVariantCodec[HierarchyType] {
    private[json] type ConcreteType <: HierarchyType

    private[json] def tt: TypeTag[ConcreteType]

    private[json] def codec: Codec[ConcreteType]

    private[json] def fieldName: String

    private[json] def fieldValue: String
  }

  private[BusymachinesDefaultJsonCodec] trait DeriveImpl {

    def enumerationCodec[T](children: T*): ValueCodec[T] = {
      if (children.distinct.size != children.size) {
        throw busymachines.core.exceptions.Error("you passed a duplicate value when instantiating an enumCodec. This kills everything.")
      }
      new ValueCodec[T] {
        private val objToCodec: Map[T, EnumValueCodec[T]] = children.map(o => (o, new EnumValueCodec[T](o))).toMap
        private val strToCodec: Map[String, EnumValueCodec[T]] = objToCodec.map { kv =>
          (kv._2.reprString, kv._2)
        }

        override def read(json: Json): T = json match {
          case JsString(s) =>
            strToCodec.applyOrElse(
              s,
              deserializationError(s"Unknown enum value: $s")
            ).read(json)

          case x => deserializationError("Enums have to be encoded as JsStrings, but got: " + x)
        }

        override def write(obj: T): Json = {
          objToCodec.applyOrElse(
            obj,
            deserializationError(s"Unknown case object: ${obj.getClass.getSimpleName}")
          ).write(obj)
        }
      }
    }

    def jsonObject[T](v: T): Codec[T] = new Codec[T] {
      override def read(json: Json): T = json match {
        case _: JsonObject => v
        case _ => deserializationError("case object expected to be JsonObject, but was: " + json)
      }

      override def write(obj: T): Json = JsonObject.empty
    }

    def adt[H](head: ADTVariantCodec[H], tail: ADTVariantCodec[H]*): Codec[H] = {
      val codecs = head :: tail.toList
      val fieldValuesToCodec: Map[String, ADTVariantCodec[H]] = codecs.map { k =>
        (k.fieldValue, k)
      }.toMap
      val fieldNameSet: Set[String] = codecs.map(_.fieldName).toSet

      if (fieldValuesToCodec.keySet.size != codecs.size) {
        throw busymachines.core.exceptions.Error(
          "you passed an ADT variant with the same discriminator as another one. This kills everything."
        )
      }

      if (fieldNameSet.size != 1) {
        throw busymachines.core.exceptions.Error(
          "just use the same field discriminator per ADT, seriously now, wtf?. This kills everything."
        )
      }

      new Codec[H] {
        private val fieldName: String = fieldNameSet.head
        private val discriminatorsToCodecs: Map[String, ADTVariantCodec[H]] = fieldValuesToCodec
        private val runtimeClassNamesToCodecs: Map[String, ADTVariantCodec[H]] = codecs.map { c =>
          (c.tt.mirror.runtimeClass(c.tt.tpe).getCanonicalName.trim, c)
        }.toMap

        override def read(json: Json): H = {
          val discriminator = {
            json.asJsObject("ADTVariant has to be a JsonObject").fields.getOrElse(
              fieldName,
              deserializationError("ADTVariant needs to have field with name:" + fieldName)
            )
          }

          discriminator match {
            case JsString(value) =>
              val jsonCodec = discriminatorsToCodecs.getOrElse(
                value,
                deserializationError(s"No such ADT with discriminator '$value'")
              )
              jsonCodec.codec.read(json)
            case _ => deserializationError(s"ADTVariant.$fieldName has to have a value of type JsString")
          }

        }

        override def write(obj: H): Json = {
          val name = obj.getClass.getCanonicalName
          val adtCodec = runtimeClassNamesToCodecs.getOrElse(
            name,
            deserializationError(s"No such json formatter for ADT: $name")
          )
          val codec: Codec[_] = adtCodec.codec

          JsonObject(
            codec.asInstanceOf[Codec[H]].write(obj).asJsObject.fields + (fieldName -> JsString(adtCodec.fieldValue))
          )
        }
      }
    }
  }

}
