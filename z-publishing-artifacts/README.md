# SBT local config example

This folder is useful to you _only_ if you are responsible for publishing this library to the Sonatype repository, otherwise it can be completely ignored.

The contents of this folder ought to be copied to your `~/.sbt/1.0` folder. And the appropriate values assigned to each sbt key.

## [plugins/plugins.sbt](plugins/plugins.sbt)

Note that for this to work you need to have [PGP installed](https://gpgtools.org/) on your machine! By defauly this build will look that it has a `pgp` command available in your `$PATH`.


Loads the [sbt-pgp](https://github.com/sbt/sbt-pgp) as a global plugin for you machine, and [pgp.sbt](pgp.sbt) contains the paths to your PGP keys, and—optionally—the path to the `gpg` command on your system if it's not available in your global `$PATH` variable`:

```scala
com.typesafe.sbt.pgp.PgpKeys.pgpSecretRing := file("~/.gnupg/secring.gpg")
com.typesafe.sbt.pgp.PgpKeys.pgpPublicRing := file("~/.gnupg/pubring.gpg")
// com.typesafe.sbt.pgp.PgpKeys.gpgCommand := "/path/to/gpg"
```

## [sonatype.sbt](sonatype.sbt)

This file contains the username and passwords for the Sonatype repository:

```scala
credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "$USERNAME_ON_SONATYPE_JIRA",
  "$PASSWORD_ON_SONATYPE_JIRA"
)
```

Currently we have only one sonatype user who uses this account, as can be seen in this issue on their JIRA:
[https://issues.sonatype.org/browse/OSSRH-33718](https://issues.sonatype.org/browse/OSSRH-33718)

## After everything is setup

You ought to follow the instructions here to create your PGP file:
[http://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks](http://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks)

## Publishing

Full workflow to [publish to sonatype](http://www.scala-sbt.org/release/docs/Using-Sonatype.html#Using+Sonatype) can be found on the sbt website, and you really need to read it all. _DO NOT FORGET_ to distribute your PGP keys to the keyserver pool by running the sbt task from `PGP Tips'n'tricks` section from the aforementione guide.

### Open staging profile

Outlined here on the [sbt-sonatype](https://github.com/xerial/sbt-sonatype/blob/master/workflow.md) plugin page.

### Then Run the commands outlined on the [sbt-sonatype](https://github.com/xerial/sbt-sonatype#publishing-your-artifact) page. Copy pasted from there for our convenience:

The general steps for publishing your artifact to the Central Repository are as follows:

 * `sonatypeOpen "com.busymachines" "combusymachines-$X"` — this lives until you do `sonatypeClose`
 * `publishSigned` to deploy your artifact to staging repository at Sonatype.
 * `sonatypeRelease` do `sonatypeClose` and `sonatypePromote` in one step.
   * `sonatypeClose` closes your staging repository at Sonatype. This step verifies Maven central sync requirement, GPG-signature, javadoc
   and source code presence, pom.xml settings, etc.
   * `sonatypePromote` command verifies the closed repository so that it can be synchronized with Maven central.


Note: If your project version has "SNAPSHOT" suffix, your project will be published to the [snapshot repository](http://oss.sonatype.org/content/repositories/snapshots) of Sonatype, and you cannot use `sonatypeRelease` command.

#### Command Line Usage

Publish a GPG-signed artifact to Sonatype:
```
$ sbt publishSigned
```

Do close and promote at once:
```
$ sbt sonatypeRelease
```
This command accesses [Sonatype Nexus REST API](https://oss.sonatype.org/nexus-staging-plugin/default/docs/index.html), then sends close and promote commands.
```
