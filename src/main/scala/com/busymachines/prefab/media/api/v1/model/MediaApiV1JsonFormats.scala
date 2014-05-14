package com.busymachines.prefab.media.api.v1.model

import com.busymachines.commons.Implicits._
import com.busymachines.prefab.media.domain.MediaDomainJsonFormats

trait MediaApiV1JsonFormats { this: MediaDomainJsonFormats =>
  implicit val mediaInputFormat = format4(MediaInput)
}


