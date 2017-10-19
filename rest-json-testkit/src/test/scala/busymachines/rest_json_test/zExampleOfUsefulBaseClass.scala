package busymachines.rest_json_test

import busymachines.rest._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 11 Sep 2017
  *
  */
abstract class ExampleRestAPITestBaseClass extends FlatSpec with JsonRestAPITest

/**
  * This can serve as an example of how to set the implicit [[testedRoute]] parameter even
  * if it is instantiated in the [[fixture.FlatSpec#withFixture]] method
  */
abstract class ExampleRestAPITestBaseClassWithFixture extends fixture.FlatSpec with JsonRestAPITest with OneInstancePerTest {
  protected[this] var _testedRoute: Route = _

  override implicit protected def testedRoute: Route = _testedRoute

  override type FixtureParam = RestAPI
}
