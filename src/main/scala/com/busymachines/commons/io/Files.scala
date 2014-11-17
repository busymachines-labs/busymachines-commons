package com.busymachines.commons.io

import java.nio.{file => jio}

import scala.util.Try

/**
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11/17/14.
 *
 * Utility class for dealing with operations on directories
 */
object Files {
  private val deleteDirectoryRecursivelyVisitor = new jio.SimpleFileVisitor[jio.Path]() {
    override def visitFile(file: jio.Path, attrs: jio.attribute.BasicFileAttributes): jio.FileVisitResult = {
      jio.Files.delete(file)
      jio.FileVisitResult.CONTINUE
    }

    override def postVisitDirectory(dir: jio.Path, exc: java.io.IOException): jio.FileVisitResult = {
      jio.Files.delete(dir)
      jio.FileVisitResult.CONTINUE
    }
  }

  def delete(path: String, options: DeleteOption = DefaultDelete): Try[Unit] = {
    val toDelete = jio.Paths.get(path)
    if (jio.Files.exists(toDelete))
      Try {
        if (toDelete.toFile.isDirectory)
          options match {
            case DeleteRecursively =>
              jio.Files.walkFileTree(toDelete, deleteDirectoryRecursivelyVisitor)
            case DefaultDelete =>
              jio.Files.delete(toDelete)
          }
        else
          jio.Files.delete(toDelete)
      }
    else Try {}
  }

  def createDirectory(path: String): Try[Unit] = {
    Try {
      jio.Files.createDirectory(jio.Paths.get(path))
    }
  }

  def createDirectories(path: String): Try[Unit] = {
    Try {
      jio.Files.createDirectories(jio.Paths.get(path))
    }
  }


}