package com.busymachines.commons.elasticsearch

import spray.json.JsObject
import scala.collection.concurrent.TrieMap


object ESMigrator {

  def migrate(index: ESIndex, migration: ESMigration) {

  }

}

/**
 * Represents an integrity check of an index from a specific version to another version.
 */
trait ESIntegrityCheck {

  private[elasticsearch] val documentCheckers = new TrieMap[String, JsObject => Option[IntegrityViolation]]

  val fromVersion: Int
  val toVersion: Int

  protected def checkDocument(typeName: String)(f: JsObject => Option[IntegrityViolation]) =
    documentCheckers += typeName -> f

  protected def checkMapping(typeName: String, mapping: ESMapping[Any])
}

case class IntegrityViolation(document: JsObject, reason: String)

/**
 * Represents a migration of an index from a specific version to another version.
 */
trait ESMigration {

  private[elasticsearch] val migrators = new TrieMap[String, JsObject => JsObject]

  val fromVersion: Int
  val toVersion: Int
  val previous: Option[ESMigration] = None

  protected def migrate(typeName: String)(f: JsObject => JsObject) =
    migrators += typeName -> f
}


object Migrate0to1 extends ESMigration {
  val fromVersion = 1
  val toVersion = 2

  migrate("customer") { doc =>
    doc
  }

}