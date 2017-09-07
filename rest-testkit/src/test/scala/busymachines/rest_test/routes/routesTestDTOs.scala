package busymachines.rest_test.routes

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
case class SomeTestDTOGet(
  int: Int,
  string: String,
  option: Option[Int]
)

case class SomeTestDTOPost(
  string: String,
  option: Option[Int]
)

case class SomeTestDTOPut(
  string: String,
  option: Option[Int]
)

case class SomeTestDTOPatch(
  string: String
)
