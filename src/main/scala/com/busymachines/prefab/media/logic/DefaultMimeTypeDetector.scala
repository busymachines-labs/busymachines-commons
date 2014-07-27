package com.busymachines.prefab.media.logic

import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.commons.domain.MimeTypes
import com.busymachines.commons.domain.MimeType

object DefaultMimeTypeDetector extends MimeTypeDetector {
	def mimeTypeOf(name:Option[String],data:Option[Array[Byte]]):Option[MimeType] =
	  name.map(MimeTypes.fromResourceName)
}