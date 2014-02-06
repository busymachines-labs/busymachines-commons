package com.busymachines.prefab.media.db

import scala.concurrent.Future
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Media
import com.busymachines.commons.domain.MimeType

trait MediaDao {

  /**
   * Retrieves all medias stored.
   * @return
   */
  def retrieveAll: Future[List[Media]]

  /**
   * Deletes media by id.
   * @param id the media id
   * @return
   */
  def delete(id: Id[Media]): Future[Unit]

  /**
   * Retrieves a media object by id
   * @param id the media id
   * @return the media or None
   */
  def retrieve(id: Id[Media]): Future[Option[Media]]

  /**
   * Retrieves media by mime type & name & hashed data
   * @param mimeType the media mime type
   * @param name the media name
   * @param data the data
   * @return the media found or None
   */
  def retrieve(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Option[Media]]

  /**
   * Stores a media object that contains the specified data. Does automatic deduplication by name, mimeType & data hash.
   * @param mimeType the media mime type
   * @param name the media name
   * @param data the data
   * @return the media found or stored
   */
  def store(mimeType: MimeType, name: Option[String], data: Array[Byte]): Future[Media]

  /**
   * Imports a media object from a url. Does automatic deduplication.
   * @param url
   * @return
   */
  def importUrl(url: String): Future[Option[Media]]

  /**
   * Reads the a byte array from the specified url.
   * @param url the url
   * @return the byte array or None (if url not found)
   */
  def readUrl(url: String): Option[Array[Byte]]

}