package busymachines.rest

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 30 Oct 2017
  *
  */
trait MinimalWebServerConfig {
  def host: String

  def port: Int

  def show: String = s"$host:$port"
}

object MinimalWebServerConfig {
  def default: MinimalWebServerConfig = MinimalWebServerConfigImpl("localhost", 9999)

  def apply(host: String, port: Int): MinimalWebServerConfig = MinimalWebServerConfigImpl(host, port)

  private case class MinimalWebServerConfigImpl(
    override val host: String,
    override val port: Int
  ) extends MinimalWebServerConfig

}
