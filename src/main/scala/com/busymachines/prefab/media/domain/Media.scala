package com.busymachines.prefab.media.domain

import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.HasId
import com.busymachines.commons.domain.MimeType

case class Media(
  id : Id[Media]  = Id.generate, 
  mimeType : MimeType, 
  name : Option[String] = None, 
  data : Array[Byte]
) extends HasId[Media]

