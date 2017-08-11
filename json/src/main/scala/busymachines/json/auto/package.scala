package busymachines.json

import scala.reflect.{ClassTag, classTag}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
package object auto extends DefaultTypeDiscriminatorConfig with io.circe.generic.extras.AutoDerivation {

  implicit def enumerationEncoder[A <: Enumeration] : Encoder[A#Value] = Encoder[String] contramap {_.toString}
  implicit def enumerationDecoder[A <: Enumeration : ClassTag]: Decoder[A#Value] = {
    Decoder.decodeString emap {str =>
      val values = implicitly[ClassTag[A]].runtimeClass.getField("MODULE$").get(null).asInstanceOf[A].values.toSeq
      val className = classTag[A].getClass.getCanonicalName

      values.find{_.toString.equalsIgnoreCase(str)} match {
        case None => Left(s"Couldn't find $str in enumeration $className")
        case Some(v) => Right(v)
      }
    }
  }
}
