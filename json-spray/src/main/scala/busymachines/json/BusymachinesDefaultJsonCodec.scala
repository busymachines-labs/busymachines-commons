package busymachines.json

import spray.json.{JsString, deserializationError, serializationError}

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
trait BusymachinesDefaultJsonCodec extends AnyRef with
  FailureMessageJsonCodec with
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

  implicit class ADTEnumCaseObjectVariantType[CaseObjectVariantType: TypeTag](co: CaseObjectVariantType) {
    def asConstant[EnumType >: CaseObjectVariantType](str: String): BusymachinesDefaultJsonCodec.ADTEnumVariantCodec[EnumType] = {
      new BusymachinesDefaultJsonCodec.ADTEnumVariantCodec[EnumType] {
        override private[json] type ConcreteType = CaseObjectVariantType

        override private[json] val tt: TypeTag[CaseObjectVariantType] = implicitly[TypeTag[CaseObjectVariantType]]

        override private[json] val stringRepr: String = str

        override private[json] val jsonRepr: Json = JsString(stringRepr)

        override private[json] val constValue: ConcreteType = co
      }

    }
  }

  val derive: BusymachinesDefaultJsonCodec.derive.type =
    BusymachinesDefaultJsonCodec.derive
}

private[json] object BusymachinesDefaultJsonCodec {

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

  trait ADTVariantCodec[HierarchyType] {
    private[json] type ConcreteType <: HierarchyType

    private[json] def tt: TypeTag[ConcreteType]

    private[json] def codec: Codec[ConcreteType]

    private[json] def fieldName: String

    private[json] def fieldValue: String
  }

  /**
    * Basically, it makes a complete un-type-safe assumption that you
    * will pass it a case object, no static guarantees whatsoever towards
    * that end.
    */
  trait ADTEnumVariantCodec[HierarchyType] {
    private[json] type ConcreteType <: HierarchyType

    private[json] def tt: TypeTag[ConcreteType]

    private[json] def stringRepr: String

    private[json] def jsonRepr: Json

    private[json] def constValue: ConcreteType
  }

  private[BusymachinesDefaultJsonCodec] trait DeriveImpl {

    def enumerationCodec[H](head: ADTEnumVariantCodec[H], tail: ADTEnumVariantCodec[H]*): ValueCodec[H] = {
      val codecs: List[ADTEnumVariantCodec[H]] = head :: tail.toList

      val constantValueToCodec: Map[H, ADTEnumVariantCodec[H]] = codecs.map(c => (c.constValue, c)).toMap

      val stringReprToCodec: Map[String, ADTEnumVariantCodec[H]] = codecs.map(c => (c.stringRepr, c)).toMap
      if (constantValueToCodec.keySet.size != codecs.size) {
        throw busymachines.core.exceptions.Error("you passed a duplicate constant case object value when instantiating an enumCodec. This kills everything.")
      }

      if (stringReprToCodec.keySet.size != codecs.size) {
        throw busymachines.core.exceptions.Error("you passed a duplicate string representation in the [[asConstant]] value of a case object when instantiating an enumCodec. This kills everything.")
      }

      new ValueCodec[H] {
        private val caseObjectToCodec: Map[H, ADTEnumVariantCodec[H]] = constantValueToCodec
        private val stringValueToCodec: Map[String, ADTEnumVariantCodec[H]] = stringReprToCodec

        override def read(json: Json): H = json match {
          case JsString(s) =>
            val c = stringValueToCodec.getOrElse(
              s,
              deserializationError(s"Unknown enum value: $s")
            )
            c.constValue

          case x => deserializationError("Enums have to be encoded as JsStrings, but got: " + x)
        }

        override def write(obj: H): Json = {
          val c = caseObjectToCodec.getOrElse(
            obj,
            serializationError(s"Unknown case obj: ${obj.getClass.getSimpleName}. Cannot serialize")
          )

          c.jsonRepr
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
