package com.busymachines.prefab.media

import com.busymachines.prefab.media.api.v1.MediasApiV1
import com.busymachines.prefab.media.elasticsearch.ESMediaDao
import com.busymachines.prefab.party.PartyAssembly
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.logic.DefaultMimeTypeDetector

trait MediaAssembly extends PartyAssembly {

  // default configuration
  def mediaIndex = index

  lazy val mediaDao = new ESMediaDao(mediaIndex,mediaMimeTypeDetector)
  lazy val mediaMimeTypeDetector:MimeTypeDetector = new DefaultMimeTypeDetector
  lazy val mediasApiV1 = new MediasApiV1(mediaDao, userAuthenticator,mediaMimeTypeDetector)

}