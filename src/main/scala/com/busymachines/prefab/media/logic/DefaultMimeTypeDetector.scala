package com.busymachines.prefab.media.logic

import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.MimeTypes
import com.busymachines.prefab.media.service.MimeTypeDetector

class DefaultMimeTypeDetector extends MimeTypeDetector {
	def mimeTypeOf(name:Option[String],data:Option[Array[Byte]]):Option[MimeType] =
	  name.map(MimeTypes.fromResourceName)
}