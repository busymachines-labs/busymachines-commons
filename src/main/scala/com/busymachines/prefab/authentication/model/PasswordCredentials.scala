package com.busymachines.prefab.authentication.model

import java.security.MessageDigest

import com.busymachines.commons.Implicits._
import scala.util.Random

object HashFunctions {
  val md5: (String => Array[Byte]) = { text: String => MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8")) }

  def sha256: (String => Array[Byte]) = { text: String => MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8")) }

}

object PasswordCredentials extends ((String, String, String) => PasswordCredentials) {

  def apply(login: String, password: String, hashFunction: (String => Array[Byte])) = {
    val salt = 0.to(12).map(_ => Random.nextPrintableChar()).mkString
    val passwordHash = hashFunction(password + salt).toHexString
    new PasswordCredentials(login, salt, passwordHash)
  }
}

case class PasswordCredentials(
  login: String,
  salt: String,
  passwordHash: String) {

  def hasPassword(password: String, hashFunction: (String => Array[Byte])) = {
    val toCompareHash = hashFunction(password + salt).toHexString
    passwordHash.equalsIgnoreCase(toCompareHash)
  }
}

