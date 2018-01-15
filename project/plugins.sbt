/**
  * Helps us publish the artifacts to sonatype, which in turn
  * pushes to maven central.
  *
  * https://github.com/xerial/sbt-sonatype
  */
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

/**
  *
  * Signs all the jars, used in conjunction with sbt-sonatype
  *
  * https://github.com/sbt/sbt-pgp
  */
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

/**
  * The best thing since sliced bread.
  *
  * https://github.com/scalameta/scalafmt
  */
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.4.0")

/**
  * Refactoring/linting tool for scala.
  *
  * https://github.com/scalacenter/scalafix
  * https://scalacenter.github.io/scalafix/
  *
  * From docs:
  * {{{
  *   // ===> sbt shell
  *
  *   > scalafixEnable                         // Setup scalafix for active session.
  *
  *   > scalafix                               // Run all rules configured in .scalafix.conf
  *
  *   > scalafix RemoveUnusedImports           // Run only RemoveUnusedImports rule
  *
  *   > myProject/scalafix RemoveUnusedImports // Run rule in one project only
  *
  * }}}
  */
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.7")
