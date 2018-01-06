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
addSbtPlugin("com.jsuereth"   % "sbt-pgp"      % "1.1.0")

/**
  * The best thing since sliced bread.
  *
  * https://github.com/scalameta/scalafmt
  */
addSbtPlugin("com.geirsson"   % "sbt-scalafmt" % "1.4.0")
