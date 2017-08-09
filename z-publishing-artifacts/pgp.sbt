com.typesafe.sbt.pgp.PgpKeys.pgpSecretRing := file("~/.gnupgp/secring.asc")
com.typesafe.sbt.pgp.PgpKeys.pgpPublicRing := file("~/.gnupgp/pubring.asc")
// com.typesafe.sbt.pgp.PgpKeys.gpgCommand := "/path/to/gpg"

//for easy copy pasting just run this in your sbt console:
// """thepasswordofthekey""".map(c => s""" '$c' """).mkString(",")
com.typesafe.sbt.pgp.PgpKeys.pgpPassphrase := Some(Array('t', 'h', 'e', 'p', 'a', 's', 'w', 'o', 'r', 'd', 'o', 'f', 't', 'h', 'e', 'k', 'e', 'y'))
