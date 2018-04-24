/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Assertion, Assertions, Suite}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  *
  * The reason why the methods [[RestAPITest#get]], etc.
  * have only the [[CallerContext]] as an implicit parameter is to reduce
  * the amount of implicit resolution that has to be done in the actual
  * test code.
  *
  * Therefore [[Route]], and [[akka.stream.ActorMaterializer]] are resolved now
  * at the method definition. And what is actually unique to the call-site
  * is left to be resolved there.
  *
  * Ideally, you'd want to resolve all at call site, but that puts too much
  * of a compilation burden on your tests. So we sacrifice a bit of test
  * flexibility for compile time.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
trait RestAPITest
    extends ScalatestRouteTest with RestAPIRequestBuildingSugar with DefaultRequestRunners with ProvidedContexts {
  this: Suite with Assertions =>

  protected implicit def testRoutingSettings: RoutingSettings =
    RoutingSettings.apply(testConfig)

  protected implicit def testedRoute: Route

  protected def printingTimeoutDuration: FiniteDuration = 2 minutes

  private[this] var _requestRunner: RequestRunner = RequestRunners.normal

  protected[this] def requestRunner: RequestRunner = _requestRunner

  protected def debug[T](thunk: => T): T = {
    if (_requestRunner eq RequestRunners.printing) {
      fail("... you should not nest debug statements. Fix it.")
    }
    _requestRunner = RequestRunners.printing
    val r = thunk
    _requestRunner = RequestRunners.normal
    r
  }

  protected def expectStatus(sc: StatusCode): Assertion = {
    assertResult(sc)(response.status)
  }
}
