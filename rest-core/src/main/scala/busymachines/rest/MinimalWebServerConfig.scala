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

import cats.Show

import scala.concurrent.duration._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 30 Oct 2017
  *
  */
trait MinimalWebServerConfig {
  def host: String
  def port: Int

  def waitAtMostForCleanup: FiniteDuration = FiniteDuration(60, SECONDS)
}

object MinimalWebServerConfig {
  def default: MinimalWebServerConfig = MinimalWebServerConfigImpl("0.0.0.0", 9999)

  def apply(host: String, port: Int): MinimalWebServerConfig = MinimalWebServerConfigImpl(host, port)

  private case class MinimalWebServerConfigImpl(
    override val host: String,
    override val port: Int
  ) extends MinimalWebServerConfig

  implicit val MinimalWebServerConfigShow: Show[MinimalWebServerConfig] =
    Show.show(c => s"/${c.host}:${c.port}")
}
