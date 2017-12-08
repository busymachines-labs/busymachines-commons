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

  def authentication: Directive1[AuthenticationResult]

  lazy val optionalAuthentication: Directive1[Option[AuthenticationResult]] =
    authentication.map(r => Option[AuthenticationResult](r)).recover { rej =>
      provide(Option.empty[AuthenticationResult])
    }
}

@SubjectToChange("0.3.0")
object RestAPIAuthentications {

  private val BasicS         = "Basic"
  private val BearerS        = "Bearer"
  private val AuthorizationS = "Authorization"

  private val MissingBasicCredentials = AuthenticationFailedRejection(
    cause     = AuthenticationFailedRejection.CredentialsMissing,
    challenge = HttpChallenges.basic(BasicS)
  )

  private val InvalidBasicCredentials = AuthenticationFailedRejection(
    cause     = AuthenticationFailedRejection.CredentialsRejected,
    challenge = HttpChallenges.basic(BasicS)
  )

  private val MissingBearerCredentials = AuthenticationFailedRejection(
    cause     = AuthenticationFailedRejection.CredentialsMissing,
    challenge = HttpChallenges.oAuth2(BearerS)
  )

  private val InvalidBearerCredentials = AuthenticationFailedRejection(
    cause     = AuthenticationFailedRejection.CredentialsRejected,
    challenge = HttpChallenges.oAuth2(BasicS)
  )

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
  trait Basic extends RestAPIAuthentication[String] {

    override lazy val authentication: Directive1[String] =
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
  }

  object Basic extends Basic

  /**
    *
    * The OAuth 2.0 Authorization Framework: Bearer Token Usage
    * See "2.1.  Authorization Request Header Field" from RFC 6750
    * {{{
    *   https://tools.ietf.org/html/rfc6750#section-2.1
    * }}}
    *
    * Used to extract the header for Bearer:
    * {{{
    *   Authorization: Bearer 123456788912309824987
    * }}}
    *
    */
  trait TokenBearer extends RestAPIAuthentication[String] {
    override lazy val authentication: Directive1[String] =
      optionalHeaderValueByName(AuthorizationS) flatMap {
        case None => reject(MissingBasicCredentials)
        case Some(tokenWithBearer) =>
          if (!tokenWithBearer.contains(BearerS))
            reject(InvalidBearerCredentials)
          else {
            val token = tokenWithBearer.replace(BearerS, "").trim
            provide(token)
          }
      }
  }

  object TokenBearer extends TokenBearer

}
