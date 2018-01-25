package busymachines.result

import busymachines.core._

/**
  *
  * In case you're wondering why we use implicit defs which instantiate a wrapper
  * class instead of defining an implicit class directly, well, mostly to keep
  * this file a bit more readable. We could have done that just as easily:
  *
  * https://docs.scala-lang.org/overviews/core/implicit-classes.html
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 09 Jan 2018
  *
  */
trait ResultTypeDefinitions {
  type Result[T]    = Either[Anomaly, T]
  type Correct[T]   = Right[Anomaly,  T]
  type Incorrect[T] = Left[Anomaly,   T]
}
