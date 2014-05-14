package com.busymachines.prefab.media.domain

import com.busymachines.commons.Implicits._

trait MediaDomainJsonFormats {
  implicit val hashMediaFormat = format5(HashedMedia)
}