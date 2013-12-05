package com.busymachines.commons.dao

import scala.concurrent.Future

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Media
import com.busymachines.commons.domain.MimeType

trait MediaDao {

  def retrieveAll: Future[List[Media]]
  def delete(id: Id[Media]): Future[Unit]
  def retrieve(id: Id[Media]): Future[Option[Media]]
  def store(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Media]
  def importUrl(url: String): Future[Option[Media]]
  def readUrl(url: String): Option[Array[Byte]]

}