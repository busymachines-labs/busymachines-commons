package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
trait Codec[A] extends Encoder[A] with Decoder[A]

object Codec {

  def apply[A](implicit instance: Codec[A]): Codec[A] = instance

  def instance[A](encode: Encoder[A], decode: Decoder[A]): Codec[A] = {
    new Codec[A] {
      private val enc = encode
      private val dec = decode

      override def apply(a: A): Json = enc(a)

      override def apply(c: HCursor): io.circe.Decoder.Result[A] = dec(c)
    }
  }
}
