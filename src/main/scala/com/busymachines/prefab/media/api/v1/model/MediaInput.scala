package com.busymachines.prefab.media.api.v1.model

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Media
import com.busymachines.commons.domain.HasId

case class MediaInput(
  id: Id[Media] = Id.generate,
  mimeType: String,
  name: Option[String] = None,
  data: String) extends HasId[Media]