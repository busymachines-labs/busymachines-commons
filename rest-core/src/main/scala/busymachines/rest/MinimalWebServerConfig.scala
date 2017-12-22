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
