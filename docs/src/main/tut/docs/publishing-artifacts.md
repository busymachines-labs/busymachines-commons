---
layout: docs
title: Publishing Artifacts
---
# SBT local config example

All references to a "folder" refer to the [z-publishing-artifacts](https://github.com/busymachines/busymachines-commons/tree/master/z-publishing-artifacts) folder from the github repo.

This folder is useful to you _only_ if you are responsible for publishing this library to the Sonatype repository, otherwise it can be completely ignored.

The contents of this folder ought to be copied to your `~/.sbt/1.0` folder. And the appropriate values assigned to each sbt key, as described below.

Each of the following sections describes the purpose of the files in the `z-publishing-artifacts` folder *once copied to `~/.sbt/1.0`*.
```
$ tree
.
├── README.md
├── pgp.sbt
├── plugins
│   └── plugins.sbt
└── sonatype.sbt
```

## [plugins/plugins.sbt](https://github.com/busymachines/busymachines-commons/blob/master/z-publishing-artifacts/plugins/plugins.sbt)

If copied in your global `sbt` config this will load the [sbt-pgp](https://github.com/sbt/sbt-pgp) as a global plugin for your machine.

Note that for this to work you need to have [PGP tools](https://gpgtools.org/) installed on your machine! By default this build will look that it has a `pgp` command available in your `$PATH`.

Content:
```scala
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0-M2")
```

## [pgp.sbt](https://github.com/busymachines/busymachines-commons/blob/master/z-publishing-artifacts/pgp.sbt)
As required by the `sbt-pgp` plugin required above, this file contains the paths to your PGP keys, and — optionally — the path to the `gpg` command on your system if it's not available in your global `$PATH` variable`:

Content:
```scala
com.typesafe.sbt.pgp.PgpKeys.pgpPublicRing := file("~/.gnupg/pubring.kbx")
// com.typesafe.sbt.pgp.PgpKeys.gpgCommand := "/path/to/gpg"

//run to ensure you have the full key id, as required by SBT: gpg --keyid-format LONG -k
usePgpKeyHex("FULL_HEX_KEY_ID_HERE")

//for easy copy pasting just run this in your sbt scala REPL (invoked by > console):
// """thepasswordofthekey""".map(c => s""" '$c' """).mkString(",")
com.typesafe.sbt.pgp.PgpKeys.pgpPassphrase := Some(Array('t', 'h', 'e', 'p', 'a', 's', 'w', 'o', 'r', 'd', 'o', 'f', 't', 'h', 'e', 'k', 'e', 'y'))
```

## [sonatype.sbt](https://github.com/busymachines/busymachines-commons/blob/master/z-publishing-artifacts/sonatype.sbt)

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
[https://issues.sonatype.org/browse/OSSRH-33718](https://issues.sonatype.org/browse/OSSRH-33718).

More users can be given access by commenting on the above JIRA issue.

## After everything is setup

You ought to follow the instructions here to create your PGP file:
[http://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks](http://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks)

## Publishing — the easy way

simply run the sbt command:
1. `doRelease`

It's an alias for the hard way described bellow if you wish to learn the details.

## Publishing — the hard way

The verbose way of publishing your artifact to the Central Repository is as follows:
1. `publishSigned`
2. `sonatypeRelease`
If this doesn't work for some reason, then read further to understand the entire process.

### Full-workflow

Full workflow to [publish to sonatype](http://www.scala-sbt.org/release/docs/Using-Sonatype.html#Using+Sonatype) can be found on the sbt website, and you really need to read it all. _DO NOT FORGET_ to distribute your PGP keys to the keyserver pool by running the sbt task from [PGP Tips'n'tricks](https://www.scala-sbt.org/release/docs/Using-Sonatype.html#PGP+Tips%E2%80%99n%E2%80%99tricks) section from the aforementione guide.

### Open staging profile

Outlined here on the [sbt-sonatype](https://github.com/xerial/sbt-sonatype/blob/master/workflow.md) plugin page.

### Then Run the commands outlined on the [sbt-sonatype](https://github.com/xerial/sbt-sonatype#publishing-your-artifact) page. Copy pasted from there for our convenience:

#### `publishSigned`

To deploy your artifact to staging repository at Sonatype. Implicitely creates a staging repository which you can view online at [https://oss.sonatype.org/#stagingRepositories](https://oss.sonatype.org/#stagingRepositories). You can login using the sonatype jira credentials you had to gain in previous steps.

#### `sonatypeRelease`

To do `sonatypeClose` and `sonatypePromote` in one step:
  * `sonatypeClose` closes your staging repository at Sonatype. This step verifies Maven central sync requirement, GPG-signature, javadoc
   and source code presence, pom.xml settings, etc.
  * `sonatypePromote` command verifies the closed repository so that it can be synchronized with Maven central.

#### [other commands](https://github.com/xerial/sbt-sonatype#available-commands)

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

#### Publish docs

Don't forget to run the `doSitePublish` task to publish your newly updated docs.