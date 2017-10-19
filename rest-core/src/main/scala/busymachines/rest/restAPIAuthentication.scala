package busymachines.rest

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 07 Sep 2017
  *
  */

import Directives._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server._
import busymachines.core.SubjectToChange

import scala.util.{Failure, Success, Try}

@SubjectToChange("0.3.0")
trait RestAPIAuthentication[AuthenticationResult] {

  protected def optionalAuthenticationHeader: Directive1[Option[AuthenticationResult]]

  protected def authenticationHeader: Directive1[AuthenticationResult]

}

@SubjectToChange("0.3.0")
object RestAPIAuthentications {

  val BasicS: String = "Basic"
  val BearerS: String = "Bearer"
  val AuthorizationS: String = "Authorization"

  final val MissingBasicCredentials = AuthenticationFailedRejection(
    cause = AuthenticationFailedRejection.CredentialsMissing,
    challenge = HttpChallenges.basic(BasicS))

  final val InvalidBasicCredentials = AuthenticationFailedRejection(
    cause = AuthenticationFailedRejection.CredentialsRejected,
    challenge = HttpChallenges.basic(BasicS))


  final val MissingBearerCredentials = AuthenticationFailedRejection(
    cause = AuthenticationFailedRejection.CredentialsMissing,
    challenge = HttpChallenges.oAuth2(BearerS))


  /**
    * See "11.1  Basic Authentication Scheme" from RFC 1945
    * {{{
    *   https://tools.ietf.org/html/rfc1945#section-11.1
    * }}}
    *
    * Used to extract the header for basic access authentication:
    * {{{
    *   Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    * }}}
    *
    */
  @SubjectToChange("0.3.0")
  trait Basic extends RestAPIAuthentication[String] {

    def authenticationHeader: Directive1[String] =
      optionalHeaderValueByName(AuthorizationS) flatMap {
        case None => reject(MissingBasicCredentials)
        case Some(encodedCredentials) =>
          if (!encodedCredentials.contains(BasicS))
            reject(InvalidBasicCredentials)
          else {
            val credentials = encodedCredentials.replace(BasicS, "").trim
            Try(java.util.Base64.getDecoder.decode(credentials)) match {
              case Success(_) => provide(credentials)
              case Failure(_) => reject(InvalidBasicCredentials)
            }
          }
      }

    override def optionalAuthenticationHeader: Directive1[Option[String]] = authenticationHeader.map(s => Option[String](s)).recover { rej =>
      provide(Option.empty[String])
    }
  }


}
