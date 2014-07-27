package com.busymachines.prefab.media

import akka.actor.ActorSystem
import com.busymachines.commons.elasticsearch.ESIndex
import com.busymachines.prefab.media.api.v1.MediasApiV1
import com.busymachines.prefab.media.elasticsearch.ESMediaDao
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.logic.DefaultMimeTypeDetector

import scala.concurrent.ExecutionContext

trait MediaAssembly {

  // dependencies
  implicit def actorSystem: ActorSystem
  implicit def executionContext: ExecutionContext
  def index: ESIndex

  // default configuration
  def mediaIndex = index

  lazy val mediaDao = new ESMediaDao(mediaIndex, mediaMimeTypeDetector)
  lazy val mediaMimeTypeDetector: MimeTypeDetector = DefaultMimeTypeDetector
  lazy val mediasApiV1 = new MediasApiV1(mediaDao, mediaMimeTypeDetector)

}