package com.busymachines.prefab.media.domain

import com.busymachines.commons.domain.CommonJsonFormats

object MediaDomainJsonFormats extends MediaDomainJsonFormats

trait MediaDomainJsonFormats extends CommonJsonFormats {
  implicit val hashMediaFormat = format5(HashedMedia)
}