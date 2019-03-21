/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
// com.typesafe.sbt.pgp.PgpKeys.pgpSecretRing := file("~/.gnupg/secring.gpg")
com.typesafe.sbt.pgp.PgpKeys.pgpPublicRing := file("~/.gnupg/pubring.kbx")
// com.typesafe.sbt.pgp.PgpKeys.gpgCommand := "/path/to/gpg"

//run to ensure you have the full key id, as required by SBT: gpg --keyid-format LONG -k
//usePgpKeyHex("FULL_HEX_KEY_ID_HERE")

//for easy copy pasting just run this in your sbt scala REPL (invoked by > console):
// """thepasswordofthekey""".map(c => s""" '$c' """).mkString(",")
com.typesafe.sbt.pgp.PgpKeys.pgpPassphrase := Some(Array('t', 'h', 'e', 'p', 'a', 's', 'w', 'o', 'r', 'd', 'o', 'f', 't', 'h', 'e', 'k', 'e', 'y'))
