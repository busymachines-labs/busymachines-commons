/**
  * See for the user accounts that can have access:
  * https://issues.sonatype.org/browse/OSSRH-33718
  */
credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "$USERNAME_ON_SONATYPE_JIRA",
  "$PASSWORD_ON_SONATYPE_JIRA"
)
