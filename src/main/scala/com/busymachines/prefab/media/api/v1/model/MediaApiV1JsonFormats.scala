package com.busymachines.prefab.media.api.v1.model

import com.busymachines.prefab.media.domain.MediaDomainJsonFormats

object MediaApiV1JsonFormats extends MediaApiV1JsonFormats

trait MediaApiV1JsonFormats extends MediaDomainJsonFormats {
  implicit val mediaInputFormat = jsonFormat4(MediaInput)
}


