package busymachines.rest_test.routes

import busymachines.rest.JsonSupport

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_test] case class SomeTestDTOGet(
  int: Int,
  string: String,
  option: Option[Int]
)

private[rest_test] case class SomeTestDTOPost(
  string: String,
  option: Option[Int]
)

private[rest_test] case class SomeTestDTOPut(
  string: String,
  option: Option[Int]
)

private[rest_test] case class SomeTestDTOPatch(
  string: String
)
