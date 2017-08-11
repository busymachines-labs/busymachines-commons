com.typesafe.sbt.pgp.PgpKeys.pgpSecretRing := file("~/.gnupg/secring.gpg")
com.typesafe.sbt.pgp.PgpKeys.pgpPublicRing := file("~/.gnupg/pubring.gpg")
// com.typesafe.sbt.pgp.PgpKeys.gpgCommand := "/path/to/gpg"

//for easy copy pasting just run this in your sbt scala REPL (invoked by > console):
// """thepasswordofthekey""".map(c => s""" '$c' """).mkString(",")
com.typesafe.sbt.pgp.PgpKeys.pgpPassphrase := Some(Array('t', 'h', 'e', 'p', 'a', 's', 'w', 'o', 'r', 'd', 'o', 'f', 't', 'h', 'e', 'k', 'e', 'y'))
