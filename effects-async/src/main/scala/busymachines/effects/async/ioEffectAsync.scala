package busymachines.effects.async

import cats.{effect => ce}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jan 2018
  *
  */
trait IOTypeDefinitions {
  type IO[T] = ce.IO[T]
  val IO: ce.IO.type = ce.IO
}
