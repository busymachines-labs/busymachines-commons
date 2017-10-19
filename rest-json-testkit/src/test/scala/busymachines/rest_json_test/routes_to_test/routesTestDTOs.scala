package busymachines.rest_json_test.routes_to_test

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */
private[rest_json_test] case class SomeTestDTOGet(
  int: Int,
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPost(
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPut(
  string: String,
  option: Option[Int]
)

private[rest_json_test] case class SomeTestDTOPatch(
  string: String
)
