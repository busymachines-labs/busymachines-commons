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
import org.scalatest.{Assertions, Suite}
import busymachines.json._
import busymachines.json.syntax._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 19 Oct 2017
  *
  */
trait JsonRestAPITest
    extends RestAPITest with JsonRequestRunners with JsonRestAPIRequestBuildingSugar with jsonrest.JsonSupport {
  this: Suite with Assertions =>
}

private[rest] trait JsonRequestRunners extends DefaultRequestRunners {
  this: ScalatestRouteTest =>

  import busymachines.json._

  override protected def transformEntityString(entityString: String): String = {
    JsonParsing.parseString(entityString) match {
      case Left(_)      => entityString
      case Right(value) => value.spaces2NoNulls
    }
  }
}

private[rest] trait JsonRestAPIRequestBuildingSugar extends RestAPIRequestBuildingSugar {
  this: ScalatestRouteTest =>

  protected def postJson[R](uri: String)(raw: Json)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Post(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }

  protected def patchJson[R](uri: String)(raw: Json)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Patch(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }

  protected def putJson[R](uri: String)(raw: Json)(thunk: => R)(implicit cc: CallerContext): R = {
    val g = Put(uri).withEntity(ContentTypes.`application/json`, raw.spaces2NoNulls)
    requestRunner.runRequest(g)(thunk)
  }
}
