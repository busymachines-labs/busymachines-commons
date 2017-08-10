package busymachines.json

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 10 Aug 2017
  *
  */
trait Codec[A] extends Encoder[A] with Decoder[A]

object Codec {
  def apply[A](implicit e: Encoder[A], d: Decoder[A]): Codec[A] = {
    new Codec[A] {
      override def apply(a: A): Json = e.apply(a)

      override def apply(c: HCursor): io.circe.Decoder.Result[A] = d.apply(c)
    }
  }
}